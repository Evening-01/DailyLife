package com.evening.dailylife.feature.chart

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class ChartType(@StringRes val labelRes: Int) {
    Expense(R.string.chart_type_expense),
    Income(R.string.chart_type_income)
}

enum class ChartPeriod(@StringRes val labelRes: Int) {
    Week(R.string.chart_period_week),
    Month(R.string.chart_period_month),
    Year(R.string.chart_period_year)
}

data class ChartEntry(
    val label: String,
    val value: Float
)

data class ChartCategoryRank(
    val category: String,
    val amount: Double,
    val ratio: Float
)

data class MoodChartEntry(
    val label: String,
    val value: Float?
)
