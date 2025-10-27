package com.evening.dailylife.core.data.repository

import com.evening.dailylife.core.data.local.dao.TransactionDao
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.local.model.DailyTransactionSummary
import com.evening.dailylife.core.data.local.model.TransactionWithDay
import com.evening.dailylife.core.di.ApplicationScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val allTransactionsState = MutableStateFlow(emptyList<TransactionEntity>())

    init {
        applicationScope.launch(Dispatchers.IO) {
            val snapshot = transactionDao.getAllTransactionsSnapshot()
                .sortedBy(TransactionEntity::date)
            allTransactionsState.value = snapshot

            transactionDao.getAllTransactions()
                .map { entities -> entities.sortedBy(TransactionEntity::date) }
                .collect { entities ->
                    allTransactionsState.value = entities
                }
        }
    }

    fun observeAllTransactions(): StateFlow<List<TransactionEntity>> {
        return allTransactionsState.asStateFlow()
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsWithDayRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDay>> {
        return transactionDao.getTransactionsWithDayRange(startDate, endDate)
    }

    fun getDailySummaries(
        startDate: Long,
        endDate: Long
    ): Flow<List<DailyTransactionSummary>> {
        return transactionDao.getDailySummaries(startDate, endDate)
    }

    fun getTransactionById(id: Int): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getTransactionsSnapshot(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsSnapshot()
            .sortedBy(TransactionEntity::date)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction.copy(isDeleted = true))
    }

    suspend fun pruneDeletedTransactions(olderThan: Long) {
        transactionDao.pruneDeletedTransactions(olderThan)
    }
}
