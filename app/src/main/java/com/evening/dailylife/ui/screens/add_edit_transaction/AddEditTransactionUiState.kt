package com.evening.dailylife.ui.screens.add_edit_transaction

data class AddEditTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val isExpense: Boolean = true, // Default to expense
    val isSaving: Boolean = false,
    val error: String? = null
)