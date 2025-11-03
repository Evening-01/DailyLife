package com.evening.dailylife.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.MonthlySnapshot
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.details.model.DailyTransactions
import com.evening.dailylife.feature.details.model.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val analyticsRepository: TransactionAnalyticsRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var monthCollectionJob: Job? = null
    private var currentCalendar: Calendar = Calendar.getInstance()

    init {
        subscribeToMonth(currentCalendar, allowFallback = true)
    }

    fun filterByMonth(calendar: Calendar) {
        currentCalendar = calendar
        subscribeToMonth(calendar, allowFallback = false)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            transactionRepository.pruneDeletedTransactions(
                System.currentTimeMillis() - SOFT_DELETE_RETENTION_MILLIS
            )
        }
    }

    private fun subscribeToMonth(calendar: Calendar, allowFallback: Boolean) {
        monthCollectionJob?.cancel()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        _uiState.value = _uiState.value.copy(isLoading = true)
        monthCollectionJob = viewModelScope.launch {
            val snapshotFlow = if (allowFallback) {
                analyticsRepository.observeLatestSnapshotUpTo(year, month)
            } else {
                analyticsRepository.observeMonthlySnapshot(year, month)
            }

            snapshotFlow.collectLatest { snapshot ->
                val shouldUpdateCalendar = allowFallback &&
                    (snapshot.year != year || snapshot.month != month)
                currentCalendar = if (shouldUpdateCalendar) {
                    Calendar.getInstance().apply {
                        clear()
                        set(Calendar.YEAR, snapshot.year)
                        set(Calendar.MONTH, snapshot.month)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                } else {
                    calendar
                }

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
            transactions = dailyTransactions,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            averageMood = averageMood,
            isLoading = false,
            displayYear = year,
            displayMonth = month
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

    companion object {
        private val SOFT_DELETE_RETENTION_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
