package com.evening.dailylife.app.main.intent

import android.content.Intent
import com.evening.dailylife.core.ui.navigation.Route

object NavigationIntent {
    const val EXTRA_NAVIGATE_ROUTE = "extra_navigate_route"
    const val EXTRA_WIDGET_IS_EXPENSE = "extra_widget_is_expense"
    const val EXTRA_WIDGET_CATEGORY_ID = "extra_widget_category_id"

    fun resolveRoute(intent: Intent?): String? {
        if (intent == null) return null
        val rawRoute = intent.getStringExtra(EXTRA_NAVIGATE_ROUTE) ?: return null
        val categoryId = intent.getStringExtra(EXTRA_WIDGET_CATEGORY_ID)?.takeIf { it.isNotBlank() }
        val isExpenseProvided = intent.hasExtra(EXTRA_WIDGET_IS_EXPENSE)
        val isExpense = if (isExpenseProvided) {
            intent.getBooleanExtra(EXTRA_WIDGET_IS_EXPENSE, true)
        } else {
            null
        }
        return if (rawRoute.startsWith(Route.ADD_EDIT_TRANSACTION_PREFIX)) {
            Route.addNewTransactionShortcut(categoryId, isExpense)
        } else {
            rawRoute
        }
    }

    fun clearExtras(intent: Intent) {
        intent.removeExtra(EXTRA_NAVIGATE_ROUTE)
        intent.removeExtra(EXTRA_WIDGET_IS_EXPENSE)
        intent.removeExtra(EXTRA_WIDGET_CATEGORY_ID)
    }
}
