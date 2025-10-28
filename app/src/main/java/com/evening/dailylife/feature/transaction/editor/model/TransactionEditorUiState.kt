package com.evening.dailylife.feature.transaction.editor.model

data class TransactionEditorUiState(
    val amount: String = "",
    val categoryId: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val isExpense: Boolean = true,
    val mood: String = "",
    val transactionId: Int? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed interface TransactionEditorEvent {
    data class ShowMessage(val message: String) : TransactionEditorEvent
    data object SaveSuccess : TransactionEditorEvent
}
