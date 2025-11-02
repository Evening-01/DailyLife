package com.evening.dailylife.app.widget

import androidx.annotation.VisibleForTesting
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.model.CategoryFlow
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.core.model.TransactionCategoryType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Immutable snapshot for the widget UI.
 */
data class TransactionWidgetState(
    val totalExpenseToday: Double,
    val totalIncomeToday: Double,
    val netToday: Double,
    val lastTransaction: TransactionWidgetSnapshot?,
    val frequentCategories: List<FrequentCategory>,
    val generatedAt: Instant
) {
    val hasTransactions: Boolean = lastTransaction != null ||
        totalExpenseToday != 0.0 ||
        totalIncomeToday != 0.0
}

data class TransactionWidgetSnapshot(
    val categoryId: String,
    val description: String,
    val amount: Double,
    val isExpense: Boolean,
    val timestamp: Instant
)

data class FrequentCategory(
    val categoryId: String,
    val isExpense: Boolean,
    val occurrences: Int,
    val averageAmount: Double
)

fun buildTransactionWidgetState(
    transactions: List<TransactionEntity>,
    zoneId: ZoneId,
    now: Instant = Instant.now()
): TransactionWidgetState {
    if (transactions.isEmpty()) {
        return TransactionWidgetState(
            totalExpenseToday = 0.0,
            totalIncomeToday = 0.0,
            netToday = 0.0,
            lastTransaction = null,
            frequentCategories = emptyList(),
            generatedAt = now
        )
    }

    val todayRange = todayRange(now, zoneId)
    val todaysTransactions = transactions.filter { entity ->
        entity.date in todayRange
    }

    val totalExpense = todaysTransactions.sumOf { entity ->
        if (entity.amount < 0) abs(entity.amount) else 0.0
    }
    val totalIncome = todaysTransactions.sumOf { entity ->
        if (entity.amount > 0) entity.amount else 0.0
    }

    val latestTransaction = transactions.maxByOrNull(TransactionEntity::date)?.let { entity ->
        val normalizedCategory = TransactionCategoryRepository.normalizeCategoryId(entity.category)
        val amount = entity.amount
        TransactionWidgetSnapshot(
            categoryId = normalizedCategory,
            description = entity.description,
            amount = abs(amount),
            isExpense = amount < 0,
            timestamp = Instant.ofEpochMilli(entity.date)
        )
    }

    val frequentCategories = computeFrequentCategories(transactions, now, zoneId)

    return TransactionWidgetState(
        totalExpenseToday = totalExpense,
        totalIncomeToday = totalIncome,
        netToday = totalIncome - totalExpense,
        lastTransaction = latestTransaction,
        frequentCategories = frequentCategories,
        generatedAt = now
    )
}

@VisibleForTesting
internal fun computeFrequentCategories(
    transactions: List<TransactionEntity>,
    now: Instant,
    zoneId: ZoneId,
    lookBackDays: Long = 30,
    maxCategories: Int = 4
): List<FrequentCategory> {
    if (transactions.isEmpty() || maxCategories <= 0) {
        return emptyList()
    }

    val cutoff = now.minus(lookBackDays, ChronoUnit.DAYS).toEpochMilli()
    val recent = transactions.filter { it.date >= cutoff }
    if (recent.isEmpty()) {
        return emptyList()
    }

    return recent
        .groupBy { TransactionCategoryRepository.normalizeCategoryId(it.category) }
        .entries
        .mapNotNull { (categoryId, entries) ->
            val flow = dominantFlowForCategory(entries)
            val relevantAmounts = entries
                .filter { entity ->
                    when (flow) {
                        CategoryFlow.EXPENSE -> entity.amount < 0
                        CategoryFlow.INCOME -> entity.amount > 0
                    }
                }
                .map { entity ->
                    when (flow) {
                        CategoryFlow.EXPENSE -> abs(entity.amount)
                        CategoryFlow.INCOME -> entity.amount
                    }
                }

            if (relevantAmounts.isEmpty()) {
                return@mapNotNull null
            }

            FrequentCategory(
                categoryId = categoryId,
                isExpense = flow == CategoryFlow.EXPENSE,
                occurrences = relevantAmounts.size,
                averageAmount = relevantAmounts.averageOrZero()
            )
        }
        .sortedWith(
            compareByDescending<FrequentCategory> { it.occurrences }
                .thenByDescending { it.averageAmount }
                .thenBy { it.categoryId }
        )
        .take(maxCategories)
}

private fun dominantFlowForCategory(
    entries: List<TransactionEntity>
): CategoryFlow {
    val expenseCount = entries.count { it.amount < 0 }
    val incomeCount = entries.count { it.amount > 0 }
    if (expenseCount == incomeCount) {
        val categoryType = TransactionCategoryType.fromValue(
            entries.firstOrNull()?.category.orEmpty()
        )
        if (categoryType != null) {
            if (CategoryFlow.EXPENSE in categoryType.flows &&
                CategoryFlow.INCOME !in categoryType.flows
            ) {
                return CategoryFlow.EXPENSE
            }
            if (CategoryFlow.INCOME in categoryType.flows &&
                CategoryFlow.EXPENSE !in categoryType.flows
            ) {
                return CategoryFlow.INCOME
            }
        }
        return if (entries.firstOrNull()?.amount ?: 0.0 <= 0.0) {
            CategoryFlow.EXPENSE
        } else {
            CategoryFlow.INCOME
        }
    }
    return if (expenseCount > incomeCount) CategoryFlow.EXPENSE else CategoryFlow.INCOME
}

private fun Iterable<Double>.averageOrZero(): Double {
    var count = 0
    var total = 0.0
    for (value in this) {
        count += 1
        total += value
    }
    return if (count == 0) 0.0 else total / count
}

private fun todayRange(now: Instant, zoneId: ZoneId): LongRange {
    val today = LocalDate.ofInstant(now, zoneId)
    val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
        .minusMillis(1)
        .toEpochMilli()
    return start..end
}
