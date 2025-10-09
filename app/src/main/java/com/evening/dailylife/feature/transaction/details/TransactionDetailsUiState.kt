package com.evening.dailylife.feature.transaction.details

import com.evening.dailylife.core.data.local.entity.TransactionEntity

data class TransactionDetailsUiState(
    val transaction: TransactionEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)