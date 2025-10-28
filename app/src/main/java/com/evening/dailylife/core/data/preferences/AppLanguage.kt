package com.evening.dailylife.core.data.preferences

import androidx.annotation.StringRes
import com.evening.dailylife.R
import java.util.Locale

enum class AppLanguage(
    @StringRes val resId: Int,
    val languageTag: String,
) {
    CHINESE(R.string.language_chinese, "zh-CN"),
    ENGLISH(R.string.language_english, "en"),
    ;

    val locale: Locale = Locale.forLanguageTag(languageTag)

    companion object {
        fun fromName(name: String?): AppLanguage {
            return runCatching { valueOf(name.orEmpty()) }.getOrDefault(CHINESE)
        }
    }
}
