package com.evening.dailylife.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTransactionsForMonth(Calendar.getInstance())
    }

    fun filterByMonth(calendar: Calendar) {
        loadTransactionsForMonth(calendar)
    }

    private fun loadTransactionsForMonth(calendar: Calendar) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

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

            repository.getTransactionsByDateRange(
                startCalendar.timeInMillis,
                endCalendar.timeInMillis
            )
                .map { transactions ->
                    val grouped = transactions.groupBy {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.timeInMillis
                    }

                    val dailyTransactions = grouped.entries
                        .sortedByDescending { it.key }
                        .map { (dateMillis, trans) ->
                            val dailyIncome = trans.filter { it.amount > 0 }.sumOf { it.amount }
                            val dailyExpense = trans.filter { it.amount < 0 }.sumOf { it.amount }

                            // 计算当天心情总分
                            val dailyMoodScore = trans.sumOf { it.mood }
                            // 根据总分获取对应的心情名称
                            val dailyMood = MoodRepository.getMoodByScore(dailyMoodScore)?.name ?: ""


                            DailyTransactions(
                                date = formatDate(dateMillis),
                                transactions = trans,
                                dailyIncome = dailyIncome,
                                dailyExpense = dailyExpense,
                                dailyMood = dailyMood // 将计算出的心情传递给UI状态
                            )
                        }

                    val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
                    val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }

                    DetailsUiState(
                        transactions = dailyTransactions,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        isLoading = false
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    private fun formatDate(dateMillis: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        calendar.timeInMillis = dateMillis

        val sdf = SimpleDateFormat("MM/dd EEEE", Locale.CHINA)

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "今天 ${sdf.format(calendar.time)}"
            else -> sdf.format(calendar.time)
        }
    }
}