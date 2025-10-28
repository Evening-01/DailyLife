package com.evening.dailylife.core.data.preferences

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class TextSizeOption(
    @StringRes val resId: Int,
    val scale: Float,
) {
    SMALL(R.string.text_size_small, 0.9f),
    MEDIUM(R.string.text_size_medium, 1.0f),
    LARGE(R.string.text_size_large, 1.1f),
    ;

    companion object {
        fun fromName(name: String?): TextSizeOption {
            return runCatching { valueOf(name.orEmpty()) }.getOrDefault(MEDIUM)
        }
    }
}
