package com.evening.dailylife.feature.discover.model

data class TypeProfile(
    val expenseTotal: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
) {
    val net: Double
        get() = incomeTotal - expenseTotal

    val total: Double
        get() = expenseTotal + incomeTotal

    val expenseRatio: Float
        get() = if (total <= 0.0) 0f else (expenseTotal / total).toFloat()

    val incomeRatio: Float
        get() = if (total <= 0.0) 0f else (incomeTotal / total).toFloat()
}
