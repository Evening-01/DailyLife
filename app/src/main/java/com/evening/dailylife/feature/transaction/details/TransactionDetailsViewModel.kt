package com.evening.dailylife.feature.transaction.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    private val transactionId: Int = savedStateHandle.get<Int>("transactionId")!!

    // 用于持有加载详情的协程任务
    private var detailsJob: Job? = null

    init {
        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        // 在开始新的加载前，取消上一个，避免不必要的更新
        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            repository.getTransactionById(transactionId).collectLatest { transaction ->
                if (transaction != null) {
                    _uiState.value = TransactionDetailsUiState(transaction = transaction, isLoading = false)
                } else {
                    // 如果UI状态不是已删除，则显示错误
                    if (_uiState.value.transaction != null) {
                        _uiState.value = TransactionDetailsUiState(error = "Transaction not found", isLoading = false)
                    }
                }
            }
        }
    }

    // 更新删除方法
    fun deleteTransaction(transaction: TransactionEntity, onDeleted: () -> Unit) {
        // 在删除前，取消对数据的监听，这是关键！
        detailsJob?.cancel()
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            // 确保导航操作在主线程执行
            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }
}