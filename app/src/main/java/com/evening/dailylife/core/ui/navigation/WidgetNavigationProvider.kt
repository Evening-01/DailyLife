package com.evening.dailylife.core.ui.navigation

import android.content.Context
import android.content.Intent

interface WidgetNavigationProvider {
    fun createOpenAppIntent(context: Context): Intent

    fun createQuickAddIntent(
        context: Context,
        route: String,
        isExpense: Boolean,
        categoryId: String?
    ): Intent
}
