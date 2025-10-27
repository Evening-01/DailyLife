package com.evening.dailylife.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.local.model.DailyTransactionSummary
import com.evening.dailylife.core.data.local.model.TransactionWithDay
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id AND isDeleted = 0")
    fun getTransactionById(id: Int): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    suspend fun getAllTransactionsSnapshot(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT 
            id AS transaction_id,
            category AS transaction_category,
            description AS transaction_description,
            amount AS transaction_amount,
            mood AS transaction_mood,
            source AS transaction_source,
            date AS transaction_date,
            isDeleted AS transaction_isDeleted,
            (date / 86400000) * 86400000 AS day_start_millis
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
        ORDER BY date DESC
        """
    )
    fun getTransactionsWithDayRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDay>>

    @Query(
        """
        SELECT 
            (date / 86400000) * 86400000 AS day_start_millis,
            SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) AS total_income,
            SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) AS total_expense,
            CAST(SUM(COALESCE(mood, 0)) AS INTEGER) AS mood_score_sum,
            SUM(CASE WHEN mood IS NOT NULL THEN 1 ELSE 0 END) AS mood_count,
            COUNT(*) AS transaction_count
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
        GROUP BY day_start_millis
        ORDER BY day_start_millis DESC
        """
    )
    fun getDailySummaries(
        startDate: Long,
        endDate: Long
    ): Flow<List<DailyTransactionSummary>>

    @Query("DELETE FROM transactions WHERE isDeleted = 1 AND date < :olderThan")
    suspend fun pruneDeletedTransactions(olderThan: Long)
}
