package com.evening.dailylife.feature.transaction.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _events = MutableSharedFlow<TransactionEditorEvent>()
    val events: SharedFlow<TransactionEditorEvent> = _events.asSharedFlow()

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category, error = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, error = null) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date, error = null) }
    }

    // 处理心情变化
    fun onMoodChange(mood: String) {
        // 如果再次点击已选中的心情，则取消选择
        val newMood = if (_uiState.value.mood == mood) "" else mood
        _uiState.update { it.copy(mood = newMood) }
    }


    fun onTransactionTypeChange(isExpense: Boolean) {
        _uiState.update {
            it.copy(
                isExpense = isExpense,
                category = "",
                error = null
            )
        }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val amountValue = currentState.amount.toDoubleOrNull()

            if (amountValue == null || amountValue == 0.0) {
                _uiState.update { it.copy(error = "请输入有效的金额") }
                _events.emit(TransactionEditorEvent.ShowMessage("请输入有效的金额"))
                return@launch
            }

            if (currentState.category.isBlank()) {
                _uiState.update { it.copy(error = "请选择一个分类") }
                _events.emit(TransactionEditorEvent.ShowMessage("请选择一个分类"))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val transactionAmount = if (currentState.isExpense) -abs(amountValue) else abs(amountValue)

            // 将心情名称转换为分数
            val moodScore = MoodRepository.moods.find { it.name == currentState.mood }?.score ?: 0


            val newTransaction = TransactionEntity(
                amount = transactionAmount,
                category = currentState.category,
                description = currentState.description,
                mood = moodScore,
                date = currentState.date,
            )

            runCatching {
                repository.insertTransaction(newTransaction)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        amount = "",
                        category = "",
                        description = "",
                        mood = "",
                        isSaving = false,
                        error = null
                    )
                }
                _events.emit(TransactionEditorEvent.SaveSuccess)
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false, error = throwable.message) }
                _events.emit(TransactionEditorEvent.ShowMessage("保存失败，请稍后重试"))
            }
        }
    }
}

sealed interface TransactionEditorEvent {
    data class ShowMessage(val message: String) : TransactionEditorEvent
    data object SaveSuccess : TransactionEditorEvent
}