package com.evening.dailylife.core.data.preferences

import android.content.Context
import com.evening.dailylife.core.appicon.AppIconManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.fastkv.FastKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
    private val appIconManager: AppIconManager,
) {

    private val fastKV: FastKV = FastKV.Builder(context, PreferencesKeys.PREFERENCES_NAME).build()
    private val _themeMode = MutableStateFlow(
        try {
            val themeName = fastKV.getString(
                PreferencesKeys.KEY_THEME_MODE,
                ThemeMode.SYSTEM.name
            ) ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode = _themeMode.asStateFlow() // 对外暴露为不可变的 StateFlow

    // 2. 将 dynamicColor 改造为 StateFlow
    private val _dynamicColor = MutableStateFlow(
        fastKV.getBoolean(PreferencesKeys.KEY_DYNAMIC_COLOR, false)
    )
    val dynamicColor = _dynamicColor.asStateFlow() // 对外暴露为不可变的 StateFlow

    private val _fingerprintLockEnabled = MutableStateFlow(
        fastKV.getBoolean(PreferencesKeys.KEY_FINGERPRINT_LOCK, false)
    )
    val fingerprintLockEnabled = _fingerprintLockEnabled.asStateFlow()

    init {
        appIconManager.applyDynamicIcon(_dynamicColor.value)
    }

    // 创建用于更新数据的方法
    fun setThemeMode(mode: ThemeMode) {
        fastKV.putString(PreferencesKeys.KEY_THEME_MODE, mode.name)
        _themeMode.value = mode // 更新 Flow 的值，通知所有观察者
    }

    fun setDynamicColor(enabled: Boolean) {
        fastKV.putBoolean(PreferencesKeys.KEY_DYNAMIC_COLOR, enabled)
        _dynamicColor.value = enabled // 更新 Flow 的值，通知所有观察者
        appIconManager.applyDynamicIcon(enabled)
    }

    fun setFingerprintLockEnabled(enabled: Boolean) {
        fastKV.putBoolean(PreferencesKeys.KEY_FINGERPRINT_LOCK, enabled)
        _fingerprintLockEnabled.value = enabled
    }

}
