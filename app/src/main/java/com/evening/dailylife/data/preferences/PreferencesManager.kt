package com.evening.dailylife.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.fastkv.FastKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _themeMode = MutableStateFlow(
        try {
            val themeName = fastKV.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode = _themeMode.asStateFlow() // 对外暴露为不可变的 StateFlow

    // 2. 将 dynamicColor 改造为 StateFlow
    private val _dynamicColor = MutableStateFlow(fastKV.getBoolean(KEY_DYNAMIC_COLOR, false))
    val dynamicColor = _dynamicColor.asStateFlow() // 对外暴露为不可变的 StateFlow

    // 3. 创建用于更新数据的方法
    fun setThemeMode(mode: ThemeMode) {
        fastKV.putString(KEY_THEME_MODE, mode.name)
        _themeMode.value = mode // 更新 Flow 的值，通知所有观察者
    }

    fun setDynamicColor(enabled: Boolean) {
        fastKV.putBoolean(KEY_DYNAMIC_COLOR, enabled)
        _dynamicColor.value = enabled // 更新 Flow 的值，通知所有观察者
    }

}