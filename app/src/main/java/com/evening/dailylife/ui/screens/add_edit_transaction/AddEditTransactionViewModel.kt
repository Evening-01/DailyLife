package com.evening.dailylife.ui.screens.add_edit_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.data.local.entity.Transaction
import com.evening.dailylife.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTransactionTypeChange(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val amountValue = currentState.amount.toDoubleOrNull()

            if (amountValue == null || amountValue == 0.0) {
                _uiState.update { it.copy(error = "请输入有效的金额") }
                return@launch
            }

            if (currentState.category.isBlank()) {
                _uiState.update { it.copy(error = "请选择一个分类") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val transactionAmount = if (currentState.isExpense) -abs(amountValue) else abs(amountValue)

            // TODO: In a real app, map category name to a specific icon string
            val iconName = when(currentState.category) {
                "餐饮" -> "Restaurant"
                "购物" -> "ShoppingCart"
                "数码" -> "Devices"
                "工资" -> "AttachMoney"
                else -> "Restaurant"
            }

            val newTransaction = Transaction(
                amount = transactionAmount,
                category = currentState.category,
                description = currentState.description,
                date = currentState.date,
                icon = iconName
            )

            repository.insertTransaction(newTransaction)

            // Saving is done (we can navigate back from the UI)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}