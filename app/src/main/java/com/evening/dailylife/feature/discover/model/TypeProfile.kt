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
        get() = when {
            incomeTotal <= 0.0 -> 0f
            else -> (expenseTotal / incomeTotal).toFloat()
        }

    val incomeRatio: Float
        get() = when {
            incomeTotal <= 0.0 -> 0f
            else -> (net / incomeTotal).coerceIn(0.0, 1.0).toFloat()
        }
}
