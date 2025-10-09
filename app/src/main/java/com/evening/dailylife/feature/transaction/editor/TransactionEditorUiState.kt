package com.evening.dailylife.feature.transaction.editor

data class TransactionEditorUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val isExpense: Boolean = true,
    val mood: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)