package com.evening.dailylife.app.widget

import com.evening.dailylife.core.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TransactionWidgetDataTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun `buildTransactionWidgetState aggregates today's totals`() {
        val today = Instant.parse("2025-10-30T08:00:00Z")
        val transactions = listOf(
            transaction(
                id = 1,
                category = "food",
                amount = -24.5,
                timestamp = today.minusSeconds(3600)
            ),
            transaction(
                id = 2,
                category = "salary",
                amount = 1200.0,
                timestamp = today.minusSeconds(1800)
            ),
            transaction(
                id = 3,
                category = "snack",
                amount = -5.0,
                timestamp = today.minusSeconds(90)
            ),
            transaction(
                id = 4,
                category = "movie",
                amount = -30.0,
                timestamp = today.minusSeconds(86_400)
            )
        )

        val state = buildTransactionWidgetState(
            transactions = transactions,
            zoneId = zoneId,
            now = today
        )

        assertEquals(29.5, state.totalExpenseToday, 0.01)
        assertEquals(1200.0, state.totalIncomeToday, 0.01)
        assertEquals(1170.5, state.netToday, 0.01)
        val last = state.lastTransaction
        assertNotNull(last)
        assertEquals("snack", last!!.categoryId)
        assertEquals(5.0, last!!.amount, 0.01)
        assertTrue(last!!.isExpense)
    }

    @Test
    fun `frequent categories favour recent high frequency entries`() {
        val now = Instant.parse("2025-10-30T12:00:00Z")
        val base = now.minusSeconds(3600)
        val transactions = buildList {
            repeat(3) { index ->
                add(transaction(id = index, category = "food", amount = -20.0, timestamp = base.minusSeconds(index * 60L)))
            }
            repeat(2) { index ->
                add(transaction(id = 10 + index, category = "shopping", amount = -80.0, timestamp = base.minusSeconds(1200L + index * 60)))
            }
            add(transaction(id = 20, category = "salary", amount = 2000.0, timestamp = base))
        }

        val state = buildTransactionWidgetState(
            transactions = transactions,
            zoneId = zoneId,
            now = now
        )

        assertTrue(state.frequentCategories.isNotEmpty())
        val topCategory = state.frequentCategories.first()
        assertEquals("food", topCategory.categoryId)
        assertTrue(topCategory.isExpense)
        assertEquals(3, topCategory.occurrences)
        assertEquals(20.0, topCategory.averageAmount, 0.01)
    }

    private fun transaction(
        id: Int,
        category: String,
        amount: Double,
        timestamp: Instant
    ): TransactionEntity = TransactionEntity(
        id = id,
        category = category,
        description = "",
        amount = amount,
        mood = null,
        source = "",
        date = timestamp.toEpochMilli(),
        isDeleted = false
    )
}