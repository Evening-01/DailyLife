package com.evening.dailylife.ui.screens.details

import com.evening.dailylife.data.local.entity.Transaction

data class DailyTransactions(
    val date: String,
    val transactions: List<Transaction>,
    val dailyIncome: Double,
    val dailyExpense: Double
)

data class DetailsUiState(
    val transactions: List<DailyTransactions> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)