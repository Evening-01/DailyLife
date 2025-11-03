package com.evening.dailylife.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.MonthlySnapshot
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.YearMonthKey
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.details.model.DailyTransactions
import com.evening.dailylife.feature.details.model.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val analyticsRepository: TransactionAnalyticsRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val initialCalendar = Calendar.getInstance()
    private var currentMonthKey = initialCalendar.toYearMonthKey()
    private var userHasManualSelection = false

    private val _uiState = MutableStateFlow(
        DetailsUiState(
            selectedYear = currentMonthKey.year,
            selectedMonth = currentMonthKey.month + 1,
            isLoading = true
        )
    )
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var monthCollectionJob: Job? = null

    init {
        subscribeToMonth(currentMonthKey)
        observeLatestTransactions()
    }

    fun filterByMonth(calendar: Calendar) {
        updateMonthSelection(calendar.toYearMonthKey(), fromUser = true)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            transactionRepository.pruneDeletedTransactions(
                System.currentTimeMillis() - SOFT_DELETE_RETENTION_MILLIS
            )
        }
    }

    private fun observeLatestTransactions() {
        viewModelScope.launch {
            transactionRepository.observeAllTransactions()
                .collectLatest { transactions ->
                    val latestKey = transactions.latestMonthKey() ?: return@collectLatest
                    val hasDataForCurrent = transactions.hasTransactionsInMonth(currentMonthKey)
                    if (!userHasManualSelection || !hasDataForCurrent) {
                        updateMonthSelection(latestKey, fromUser = false)
                    }
                }
        }
    }

    private fun updateMonthSelection(
        key: YearMonthKey,
        fromUser: Boolean
    ) {
        if (fromUser) {
            userHasManualSelection = true
        }
        if (currentMonthKey == key) {
            return
        }
        currentMonthKey = key
        subscribeToMonth(key)
    }

    private fun subscribeToMonth(key: YearMonthKey) {
        monthCollectionJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedYear = key.year,
            selectedMonth = key.month + 1,
            isLoading = true
        )
        monthCollectionJob = viewModelScope.launch {
            analyticsRepository.observeMonthlySnapshot(key.year, key.month)
                .collectLatest { snapshot ->
                    _uiState.value = snapshot.toUiState(stringProvider)
                }
        }
    }

    private fun MonthlySnapshot.toUiState(
        stringProvider: StringProvider
    ): DetailsUiState {
        val dailyTransactions = days.map { snapshot ->
            val moodText = if (snapshot.moodCount > 0) {
                MoodRepository
                    .getMoodByScore(snapshot.moodScoreSum)
                    ?.let { mood -> stringProvider.getString(mood.nameRes) }
                    ?: ""
            } else {
                ""
            }
            DailyTransactions(
                date = formatDate(snapshot.dayStartMillis),
                transactions = snapshot.transactions,
                dailyIncome = snapshot.income,
                dailyExpense = snapshot.expense,
                dailyMood = moodText
            )
        }

        return DetailsUiState(
            selectedYear = year,
            selectedMonth = month + 1,
            transactions = dailyTransactions,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            averageMood = averageMood,
            isLoading = false
        )
    }

    private fun formatDate(dateMillis: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        calendar.timeInMillis = dateMillis

        val sdf = SimpleDateFormat("MM/dd EEEE", Locale.CHINA)

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> stringProvider.getString(
                R.string.details_today_with_date,
                sdf.format(calendar.time)
            )
            else -> sdf.format(calendar.time)
        }
    }

    private fun Calendar.toYearMonthKey(): YearMonthKey {
        return YearMonthKey(
            year = get(Calendar.YEAR),
            month = get(Calendar.MONTH)
        )
    }

    private fun List<TransactionEntity>.latestMonthKey(): YearMonthKey? {
        if (isEmpty()) return null
        val calendar = Calendar.getInstance()
        val latestDate = maxOf(TransactionEntity::date)
        calendar.timeInMillis = latestDate
        return calendar.toYearMonthKey()
    }

    private fun List<TransactionEntity>.hasTransactionsInMonth(
        key: YearMonthKey
    ): Boolean {
        if (isEmpty()) return false
        val calendar = Calendar.getInstance()
        for (transaction in this) {
            calendar.timeInMillis = transaction.date
            val yearMatches = calendar.get(Calendar.YEAR) == key.year
            val monthMatches = calendar.get(Calendar.MONTH) == key.month
            if (yearMatches && monthMatches) {
                return true
            }
        }
        return false
    }

    companion object {
        private val SOFT_DELETE_RETENTION_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
