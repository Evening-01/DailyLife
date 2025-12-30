package com.evening.dailylife.core.model

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
            total <= 0.0 -> 0f
            else -> (expenseTotal / total).toFloat()
        }

    val incomeRatio: Float
        get() = when {
            total <= 0.0 -> 0f
            else -> (incomeTotal / total).toFloat()
        }
}
