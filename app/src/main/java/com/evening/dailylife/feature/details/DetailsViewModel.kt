package com.evening.dailylife.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions()
                .map { transactions ->
                    // Group transactions by day and calculate totals
                    val grouped = transactions.groupBy {
                        // Normalize date to the start of the day
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.timeInMillis
                    }

                    val dailyTransactions = grouped.map { (dateMillis, trans) ->
                        val dailyIncome = trans.filter { it.amount > 0 }.sumOf { it.amount }
                        val dailyExpense = trans.filter { it.amount < 0 }.sumOf { it.amount }
                        DailyTransactions(
                            date = formatDate(dateMillis),
                            transactions = trans,
                            dailyIncome = dailyIncome,
                            dailyExpense = dailyExpense
                        )
                    }.sortedByDescending { it.date }

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