package com.evening.dailylife.core.data.preferences

data class UserPreferencesSnapshot(
    val themeMode: String,
    val dynamicColor: Boolean,
    val fingerprintLockEnabled: Boolean,
    val uiScale: Float,
    val fontScale: Float,
    val customFontEnabled: Boolean,
    val quickUsageReminderEnabled: Boolean,
    val quickUsageReminderTimeMinutes: Int,
    val languageCode: String,
)
