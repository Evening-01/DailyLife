package com.evening.dailylife.feature.details.model

import com.evening.dailylife.core.data.local.entity.TransactionEntity
import java.util.Calendar

data class DailyTransactions(
    val date: String,
    val transactions: List<TransactionEntity>,
    val dailyIncome: Double,
    val dailyExpense: Double,
    val dailyMood: String,
)

data class DetailsUiState(
    val transactions: List<DailyTransactions> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val averageMood: Int? = null,
    val isLoading: Boolean = true,
    val displayYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val displayMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
)
