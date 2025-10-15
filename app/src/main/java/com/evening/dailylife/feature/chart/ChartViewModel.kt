package com.evening.dailylife.feature.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.util.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeTransactions()
    }

    fun onTypeSelected(type: ChartType) {
        if (type == _uiState.value.selectedType) return
        _uiState.value = _uiState.value.copy(selectedType = type)
        observeTransactions()
    }

    fun onPeriodSelected(period: ChartPeriod) {
        if (period == _uiState.value.selectedPeriod) return
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        observeTransactions()
    }

    private fun observeTransactions() {
        observeJob?.cancel()
        val type = _uiState.value.selectedType
        val period = _uiState.value.selectedPeriod
        val range = ChartDataCalculator.buildRange(period, stringProvider)

        _uiState.value = _uiState.value.copy(isLoading = true)

        observeJob = viewModelScope.launch {
            transactionRepository
                .getTransactionsByDateRange(range.start, range.end)
                .map { transactions ->
                    ChartDataCalculator.summarize(transactions, type, range)
                }
                .collectLatest { summary ->
                    _uiState.value = _uiState.value.copy(
                        entries = summary.entries,
                        totalAmount = summary.total,
                        averageAmount = summary.average,
                        isLoading = false
                    )
                }
        }
    }
}
