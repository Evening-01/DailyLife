package com.evening.dailylife.feature.transaction.details.model

import com.evening.dailylife.core.data.local.entity.TransactionEntity

data class TransactionDetailsUiState(
    val isLoading: Boolean = true,
    val transaction: TransactionEntity? = null,
    val error: String? = null,
)
