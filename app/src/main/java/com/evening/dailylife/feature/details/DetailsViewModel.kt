package com.evening.dailylife.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.details.model.DailyTransactions
import com.evening.dailylife.feature.details.model.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val allTransactionsState = repository.observeAllTransactions()

    private var loadJob: Job? = null
    private var currentCalendar: Calendar = Calendar.getInstance()

    init {
        loadTransactionsForMonth(currentCalendar)
    }

    fun filterByMonth(calendar: Calendar) {
        currentCalendar = calendar
        loadTransactionsForMonth(calendar)
    }

    // 添加删除事务的方法
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            // 删除后无需手动刷新，Room 的 Flow 会自动通知更新
            repository.pruneDeletedTransactions(
                System.currentTimeMillis() - SOFT_DELETE_RETENTION_MILLIS
            )
        }
    }

    private fun loadTransactionsForMonth(calendar: Calendar) {
        loadJob?.cancel()
        val (startMillis, endMillis) = calculateMonthBounds(calendar)

        val cachedTransactions = allTransactionsState.value
        _uiState.value = buildMonthSnapshot(calendar, cachedTransactions)

        loadJob = viewModelScope.launch {
            repository.getTransactionsWithDayRange(startMillis, endMillis)
                .combine(repository.getDailySummaries(startMillis, endMillis)) { transactions, summaries ->
                    val summaryByDay = summaries.associateBy { it.dayStartMillis }
                    val dailyTransactions = transactions
                        .groupBy { it.dayStartMillis }
                        .entries
                        .sortedByDescending { it.key }
                        .map { (dayStartMillis, items) ->
                            val summary = summaryByDay[dayStartMillis]
                            val dailyMood = summary
                                ?.takeIf { it.moodCount > 0 }
                                ?.let {
                                    MoodRepository.getMoodByScore(it.moodScoreSum)?.let { mood ->
                                        stringProvider.getString(mood.nameRes)
                                    }
                                }
                                ?: ""

                            DailyTransactions(
                                date = formatDate(dayStartMillis),
                                transactions = items.map { it.transaction },
                                dailyIncome = summary?.totalIncome ?: 0.0,
                                dailyExpense = summary?.totalExpense ?: 0.0,
                                dailyMood = dailyMood
                            )
                        }

                    val totalIncome = summaries.sumOf { it.totalIncome }
                    val totalExpense = summaries.sumOf { it.totalExpense }
                    val totalMoodScore = summaries.sumOf { it.moodScoreSum }
                    val totalMoodCount = summaries.sumOf { it.moodCount }
                    val averageMood = if (totalMoodCount > 0) {
                        totalMoodScore / totalMoodCount
                    } else {
                        null
                    }

                    DetailsUiState(
                        transactions = dailyTransactions,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        averageMood = averageMood,
                        isLoading = false
                    )
                }
                .flowOn(Dispatchers.Default)
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Surface minimal signal while avoiding crash; upstream should handle logging.
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    private fun calculateMonthBounds(calendar: Calendar): Pair<Long, Long> {
        val startCalendar = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return startCalendar.timeInMillis to endCalendar.timeInMillis
    }

    private fun buildMonthSnapshot(
        calendar: Calendar,
        allTransactions: List<TransactionEntity>
    ): DetailsUiState {
        val (startMillis, endMillis) = calculateMonthBounds(calendar)
        val monthTransactions = allTransactions.filter { transaction ->
            transaction.date in startMillis..endMillis
        }
        if (monthTransactions.isEmpty()) {
            return DetailsUiState(
                transactions = emptyList(),
                totalIncome = 0.0,
                totalExpense = 0.0,
                averageMood = null,
                isLoading = false
            )
        }

        val groupedByDay = monthTransactions.groupBy { dayStartMillis(it.date) }
        val dailyTransactions = groupedByDay.entries
            .sortedByDescending { it.key }
            .map { (dayStartMillis, items) ->
                val dailyIncome = items.filter { it.amount > 0 }.sumOf { it.amount }
                val dailyExpense = items.filter { it.amount < 0 }.sumOf { it.amount }
                val moodScoreSum = items.sumOf { it.mood ?: 0 }
                val moodCount = items.count { it.mood != null }
                val dailyMood = if (moodCount > 0) {
                    MoodRepository.getMoodByScore(moodScoreSum)?.let { mood ->
                        stringProvider.getString(mood.nameRes)
                    } ?: ""
                } else {
                    ""
                }

                DailyTransactions(
                    date = formatDate(dayStartMillis),
                    transactions = items.sortedByDescending { it.date },
                    dailyIncome = dailyIncome,
                    dailyExpense = dailyExpense,
                    dailyMood = dailyMood
                )
            }

        val totalIncome = monthTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = monthTransactions.filter { it.amount < 0 }.sumOf { it.amount }
        val totalMoodScore = monthTransactions.sumOf { it.mood ?: 0 }
        val totalMoodCount = monthTransactions.count { it.mood != null }
        val averageMood = if (totalMoodCount > 0) totalMoodScore / totalMoodCount else null

        return DetailsUiState(
            transactions = dailyTransactions,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            averageMood = averageMood,
            isLoading = false
        )
    }

    private fun dayStartMillis(timestamp: Long): Long {
        return (timestamp / MILLIS_IN_DAY) * MILLIS_IN_DAY
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
        private val SOFT_DELETE_RETENTION_MILLIS =
            TimeUnit.DAYS.toMillis(30)
        private const val MILLIS_IN_DAY = 86_400_000L
    }
}
