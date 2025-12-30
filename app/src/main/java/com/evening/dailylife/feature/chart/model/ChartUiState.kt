package com.evening.dailylife.feature.chart.model

import com.evening.dailylife.core.domain.chart.model.ChartCategoryRank
import com.evening.dailylife.core.domain.chart.model.ChartEntry
import com.evening.dailylife.core.domain.chart.model.ChartPeriod
import com.evening.dailylife.core.domain.chart.model.ChartRangeOption
import com.evening.dailylife.core.domain.chart.model.ChartType
import com.evening.dailylife.core.domain.chart.model.MoodChartEntry
import com.evening.dailylife.core.ui.model.ChartContentStatus

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
    val contentStatus: ChartContentStatus = ChartContentStatus.Loading,
)
