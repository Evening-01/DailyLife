package com.evening.dailylife.feature.transaction.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    private val transactionId: Int = savedStateHandle.get<Int>("transactionId")!!

    init {
        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        viewModelScope.launch {
            repository.getTransactionById(transactionId).collectLatest { transaction ->
                if (transaction != null) {
                    _uiState.value = TransactionDetailsUiState(transaction = transaction, isLoading = false)
                } else {
                    _uiState.value = TransactionDetailsUiState(error = "Transaction not found", isLoading = false)
                }
            }
        }
    }
}