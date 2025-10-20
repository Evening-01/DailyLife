package com.evening.dailylife.feature.chart.model

data class ChartRangeOption(
    val id: String,
    val period: ChartPeriod,
    val label: String,
    val startInclusive: Long,
    val endInclusive: Long,
)
