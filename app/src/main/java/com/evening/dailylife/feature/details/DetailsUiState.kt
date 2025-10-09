package com.evening.dailylife.feature.details

import com.evening.dailylife.core.data.local.entity.TransactionEntity

data class DailyTransactions(
    val date: String,
    val transactions: List<TransactionEntity>,
    val dailyIncome: Double,
    val dailyExpense: Double,
    val dailyMood: String // 新增字段，用于存放当天的主要心情
)

data class DetailsUiState(
    val transactions: List<DailyTransactions> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)