package com.evening.dailylife.core.data.local.model

import androidx.room.ColumnInfo

data class DailyTransactionSummary(
    @ColumnInfo(name = "day_start_millis")
    val dayStartMillis: Long,
    @ColumnInfo(name = "total_income")
    val totalIncome: Double,
    @ColumnInfo(name = "total_expense")
    val totalExpense: Double,
    @ColumnInfo(name = "mood_score_sum")
    val moodScoreSum: Int,
    @ColumnInfo(name = "mood_count")
    val moodCount: Int,
    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int
)
