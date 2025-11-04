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

    private val _uiScale = MutableStateFlow(readUiScale())
    val uiScale = _uiScale.asStateFlow()

    private val _fontScale = MutableStateFlow(readFontScale())
    val fontScale = _fontScale.asStateFlow()

    private val _customFontEnabled = MutableStateFlow(
        fastKV.getBoolean(PreferencesKeys.KEY_CUSTOM_FONT, true)
    )
    val customFontEnabled = _customFontEnabled.asStateFlow()

    private val _quickUsageReminderEnabled = MutableStateFlow(
        fastKV.getBoolean(PreferencesKeys.KEY_QUICK_USAGE_REMINDER_ENABLED, false)
    )
    val quickUsageReminderEnabled = _quickUsageReminderEnabled.asStateFlow()

    private val _quickUsageReminderTimeMinutes = MutableStateFlow(readQuickUsageReminderMinutes())
    val quickUsageReminderTimeMinutes = _quickUsageReminderTimeMinutes.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(
        fastKV.getLong(PreferencesKeys.KEY_LAST_BACKUP_TIMESTAMP, 0L)
    )
    val lastBackupTimestamp = _lastBackupTimestamp.asStateFlow()

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

    fun setUiScale(scale: Float, persist: Boolean) {
        val clamped = scale.coerceIn(0.5f, 2.0f)
        if (persist) {
            fastKV.putFloat(PreferencesKeys.KEY_UI_SCALE, clamped)
            fastKV.remove(PreferencesKeys.KEY_TEXT_SIZE)
        }
        _uiScale.value = clamped
    }

    fun setFontScale(scale: Float, persist: Boolean) {
        val clamped = scale.coerceIn(0.9f, 1.2f)
        if (persist) {
            fastKV.putFloat(PreferencesKeys.KEY_FONT_SCALE, clamped)
            fastKV.remove(PreferencesKeys.KEY_TEXT_SIZE)
        }
        _fontScale.value = clamped
    }

    fun setCustomFontEnabled(enabled: Boolean) {
        fastKV.putBoolean(PreferencesKeys.KEY_CUSTOM_FONT, enabled)
        _customFontEnabled.value = enabled
    }

    fun setFingerprintLockEnabled(enabled: Boolean) {
        fastKV.putBoolean(PreferencesKeys.KEY_FINGERPRINT_LOCK, enabled)
        _fingerprintLockEnabled.value = enabled
    }

    fun setQuickUsageReminderEnabled(enabled: Boolean) {
        fastKV.putBoolean(PreferencesKeys.KEY_QUICK_USAGE_REMINDER_ENABLED, enabled)
        _quickUsageReminderEnabled.value = enabled
    }

    fun setQuickUsageReminderTimeMinutes(minutes: Int) {
        val normalized = minutes.coerceIn(0, QuickUsageReminderDefaults.MINUTES_PER_DAY - 1)
        fastKV.putInt(PreferencesKeys.KEY_QUICK_USAGE_REMINDER_TIME_MINUTES, normalized)
        _quickUsageReminderTimeMinutes.value = normalized
    }

    fun setLastBackupTimestamp(timestamp: Long) {
        fastKV.putLong(PreferencesKeys.KEY_LAST_BACKUP_TIMESTAMP, timestamp)
        _lastBackupTimestamp.value = timestamp
    }

    private fun readUiScale(): Float {
        val stored = fastKV.getFloat(PreferencesKeys.KEY_UI_SCALE, Float.NaN)
        if (!stored.isNaN() && stored != 1.0f) {
            fastKV.putFloat(PreferencesKeys.KEY_UI_SCALE, 1.0f)
        }
        return 1.0f
    }

    private fun readFontScale(): Float {
        val stored = fastKV.getFloat(PreferencesKeys.KEY_FONT_SCALE, Float.NaN)
        if (!stored.isNaN() && stored in 0.9f..1.2f) {
            return stored
        }
        val legacy = fastKV.getString(PreferencesKeys.KEY_TEXT_SIZE, null)
        return when (legacy) {
            "SMALL" -> 0.9f
            "LARGE" -> 1.1f
            else -> 1.0f
        }
    }

    private fun readQuickUsageReminderMinutes(): Int {
        val stored = fastKV.getInt(
            PreferencesKeys.KEY_QUICK_USAGE_REMINDER_TIME_MINUTES,
            QuickUsageReminderDefaults.DEFAULT_TIME_MINUTES,
        )
        return stored.coerceIn(0, QuickUsageReminderDefaults.MINUTES_PER_DAY - 1)
    }
}
