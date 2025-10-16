package com.evening.dailylife.feature.chart

data class ChartUiState(
    val selectedType: ChartType = ChartType.Expense,
    val selectedPeriod: ChartPeriod = ChartPeriod.Week,
    val rangeTabs: List<ChartRangeOption> = emptyList(),
    val selectedRangeOption: ChartRangeOption? = null,
    val entries: List<ChartEntry> = emptyList(),
    val categoryRanks: List<ChartCategoryRank> = emptyList(),
    val totalAmount: Double = 0.0,
    val averageAmount: Double = 0.0,
    val moodEntries: List<MoodChartEntry> = emptyList(),
    val isLoading: Boolean = true
)
