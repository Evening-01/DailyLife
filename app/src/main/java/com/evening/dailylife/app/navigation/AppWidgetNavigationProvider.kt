package com.evening.dailylife.app.navigation

import android.content.Context
import android.content.Intent
import com.evening.dailylife.app.main.MainActivity
import com.evening.dailylife.app.main.intent.NavigationIntent
import com.evening.dailylife.core.ui.navigation.WidgetNavigationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetNavigationProvider @Inject constructor() : WidgetNavigationProvider {
    override fun createOpenAppIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun createQuickAddIntent(
        context: Context,
        route: String,
        isExpense: Boolean,
        categoryId: String?
    ): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra(NavigationIntent.EXTRA_NAVIGATE_ROUTE, route)
            putExtra(NavigationIntent.EXTRA_WIDGET_IS_EXPENSE, isExpense)
            putExtra(NavigationIntent.EXTRA_WIDGET_CATEGORY_ID, categoryId)
        }
    }
}
