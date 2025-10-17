package com.evening.dailylife.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                val now = Calendar.getInstance(Locale.getDefault())
                val monthStart = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    setToStartOfDay()
                }
                val monthEnd = (monthStart.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    setToEndOfDay()
                }
                val monthTransactions = transactions.filter { entity ->
                    entity.date in monthStart.timeInMillis..monthEnd.timeInMillis
                }

                _uiState.value = DiscoverUiState(
                    typeProfile = buildTypeProfile(monthTransactions),
                    isLoading = false,
                    year = monthStart.get(Calendar.YEAR),
                    month = monthStart.get(Calendar.MONTH) + 1
                )
            }
        }
    }

    private fun buildTypeProfile(
        transactions: List<TransactionEntity>
    ): TypeProfile {
        if (transactions.isEmpty()) return TypeProfile()

        var expenseTotal = 0.0
        var incomeTotal = 0.0
        var expenseCount = 0
        var incomeCount = 0

        transactions.forEach { entity ->
            when {
                entity.amount < 0 -> {
                    expenseTotal += abs(entity.amount)
                    expenseCount++
                }
                entity.amount > 0 -> {
                    incomeTotal += entity.amount
                    incomeCount++
                }
            }
        }

        return TypeProfile(
            expenseTotal = expenseTotal,
            incomeTotal = incomeTotal,
            expenseCount = expenseCount,
            incomeCount = incomeCount
        )
    }
}

private fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun Calendar.setToEndOfDay() {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}
