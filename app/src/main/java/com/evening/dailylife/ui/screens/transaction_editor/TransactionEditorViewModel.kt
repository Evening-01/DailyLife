package com.evening.dailylife.ui.screens.transaction_editor

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
class TransactionEditorViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditorUiState())
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()

    // --- 原有方法 ---
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

    // --- 新增的计算器处理方法 ---
    fun onCalculatorDigit(digit: String) {
        val currentAmount = _uiState.value.amount
        if (currentAmount == "0") {
            _uiState.update { it.copy(amount = digit) }
        } else {
            // 限制长度，避免过长
            if (currentAmount.length < 10) {
                _uiState.update { it.copy(amount = currentAmount + digit) }
            }
        }
    }

    fun onCalculatorDecimal() {
        if (!_uiState.value.amount.contains(".")) {
            _uiState.update { it.copy(amount = _uiState.value.amount + ".") }
        }
    }

    fun onCalculatorBackspace() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            _uiState.update { it.copy(amount = currentAmount.dropLast(1)) }
        }
        // 如果删除后为空，则重置为 "0"
        if (_uiState.value.amount.isEmpty()) {
            _uiState.update { it.copy(amount = "0") }
        }
    }

    // --- 原有的保存逻辑 ---
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

            _uiState.update { it.copy(isSaving = false) }
        }
    }
}