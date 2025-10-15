package com.evening.dailylife.feature.chart

data class ChartUiState(
    val selectedType: ChartType = ChartType.Expense,
    val selectedPeriod: ChartPeriod = ChartPeriod.Week,
    val entries: List<ChartEntry> = emptyList(),
    val totalAmount: Double = 0.0,
    val averageAmount: Double = 0.0,
    val isLoading: Boolean = true
)