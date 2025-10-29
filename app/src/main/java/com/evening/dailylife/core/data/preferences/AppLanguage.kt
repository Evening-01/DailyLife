package com.evening.dailylife.core.data.preferences

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class AppLanguage(
    @StringRes val resId: Int,
    val persistedCode: String,
) {
    SYSTEM(R.string.language_system, ""),
    CHINESE(R.string.language_chinese, "zh-CN"),
    ENGLISH(R.string.language_english, "en"),
    ;

    companion object {
        fun fromPersistedCode(code: String?): AppLanguage {
            if (code.isNullOrBlank()) return SYSTEM
            return entries.firstOrNull { entry ->
                entry.persistedCode.equals(code, ignoreCase = true)
            } ?: SYSTEM
        }
    }
}
