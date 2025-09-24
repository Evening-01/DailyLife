package com.evening.dailylife.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.fastkv.FastKV
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val fastKV: FastKV = FastKV.Builder(context, PREFERENCES_NAME).build()

    companion object {
        private const val PREFERENCES_NAME = "user_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }

    var themeMode: ThemeMode
        get() {
            val themeName = fastKV.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            return ThemeMode.valueOf(themeName)
        }
        set(value) {
            fastKV.putString(KEY_THEME_MODE, value.name)
        }

    var dynamicColor: Boolean
        get() = fastKV.getBoolean(KEY_DYNAMIC_COLOR, false)
        set(value) {
            fastKV.putBoolean(KEY_DYNAMIC_COLOR, value)
        }
}