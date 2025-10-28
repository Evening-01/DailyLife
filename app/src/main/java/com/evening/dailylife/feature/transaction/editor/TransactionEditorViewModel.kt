package com.evening.dailylife.feature.transaction.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.core.model.TransactionSource
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.transaction.editor.model.TransactionEditorEvent
import com.evening.dailylife.feature.transaction.editor.model.TransactionEditorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val stringProvider: StringProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditorUiState())
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransactionEditorEvent>()
    val events: SharedFlow<TransactionEditorEvent> = _events.asSharedFlow()

    private val editingTransactionId: Int? =
        savedStateHandle.get<Int>("transactionId")?.takeIf { it != -1 }

    private var originalTransaction: TransactionEntity? = null

    init {
        editingTransactionId?.let { id ->
            loadTransaction(id)
        }
    }

    private fun loadTransaction(transactionId: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId).firstOrNull()
            transaction?.let { entity ->
                val normalizedCategoryId = TransactionCategoryRepository.normalizeCategoryId(entity.category)
                val sanitizedTransaction = if (normalizedCategoryId != entity.category) {
                    entity.copy(category = normalizedCategoryId)
                } else {
                    entity
                }
                originalTransaction = sanitizedTransaction

                val moodName = entity.mood?.let { score ->
                    MoodRepository.getMoodNameByScore(stringProvider, score)
                } ?: ""

                _uiState.update {
                    it.copy(
                        amount = formatAmountForInput(abs(entity.amount)),
                        categoryId = normalizedCategoryId,
                        description = entity.description,
                        date = entity.date,
                        isExpense = entity.amount < 0,
                        mood = moodName,
                        transactionId = entity.id,
                        isEditing = true
                    )
                }
            }
        }
    }

    private fun formatAmountForInput(amount: Double): String {
        val df = DecimalFormat("0.##")
        df.isGroupingUsed = false
        return df.format(amount)
    }


    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId, error = null) }
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
                categoryId = "",
                error = null
            )
        }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val amountValue = currentState.amount.toDoubleOrNull()

            if (amountValue == null || amountValue == 0.0) {
                val error = stringProvider.getString(R.string.editor_error_invalid_amount)
                _uiState.update { it.copy(error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
                return@launch
            }

            if (currentState.categoryId.isBlank()) {
                val error = stringProvider.getString(R.string.editor_error_select_category)
                _uiState.update { it.copy(error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val transactionAmount = if (currentState.isExpense) -abs(amountValue) else abs(
                amountValue
            )

            // 将心情名称转换为分数，如果未选择心情，则为 null
            val moodScore = currentState.mood.takeIf { it.isNotEmpty() }?.let {
                MoodRepository.getMoodScoreByName(stringProvider, it)
            }


            val normalizedSource = originalTransaction?.source?.takeUnless {
                TransactionSource.isAppSource(it)
            } ?: TransactionSource.DEFAULT

            val newTransaction = originalTransaction?.copy(
                amount = transactionAmount,
                category = currentState.categoryId,
                description = currentState.description,
                mood = moodScore,
                source = normalizedSource,
                date = currentState.date
            ) ?: TransactionEntity(
                amount = transactionAmount,
                category = currentState.categoryId,
                description = currentState.description,
                mood = moodScore,
                source = normalizedSource,
                date = currentState.date,
            )

            runCatching {
                if (currentState.isEditing && currentState.transactionId != null) {
                    repository.updateTransaction(newTransaction)
                } else {
                    repository.insertTransaction(newTransaction)
                }
            }.onSuccess {
                if (currentState.isEditing) {
                    originalTransaction = newTransaction
                } else {
                    originalTransaction = null
                }
                if (currentState.isEditing) {
                    _uiState.update { it.copy(isSaving = false, error = null) }
                } else {
                    _uiState.update {
                        it.copy(
                            amount = "",
                            categoryId = "",
                            description = "",
                            mood = "",
                            isSaving = false,
                            error = null
                        )
                    }
                }
                _events.emit(TransactionEditorEvent.SaveSuccess)
            }.onFailure { throwable ->
                val error = throwable.message
                    ?: stringProvider.getString(R.string.editor_error_save_failed)
                _uiState.update { it.copy(isSaving = false, error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
            }
        }
    }
}
