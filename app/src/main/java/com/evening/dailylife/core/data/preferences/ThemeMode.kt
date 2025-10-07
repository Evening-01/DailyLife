package com.evening.dailylife.core.data.preferences

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class ThemeMode(@StringRes val resId: Int) {
    SYSTEM(R.string.theme_mode_system),
    LIGHT(R.string.theme_mode_light),
    DARK(R.string.theme_mode_dark)
}