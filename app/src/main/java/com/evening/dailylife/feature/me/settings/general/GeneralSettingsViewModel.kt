package com.evening.dailylife.feature.me.settings.general

import androidx.lifecycle.ViewModel
import com.evening.dailylife.core.data.preferences.AppLanguage
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.data.preferences.TextSizeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val dynamicColor = preferencesManager.dynamicColor
    val themeMode = preferencesManager.themeMode
    val textSizeOption = preferencesManager.textSizeOption
    val appLanguage = preferencesManager.appLanguage
    val customFontEnabled = preferencesManager.customFontEnabled

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun setTextSizeOption(option: TextSizeOption) {
        preferencesManager.setTextSizeOption(option)
    }

    fun setAppLanguage(language: AppLanguage) {
        preferencesManager.setAppLanguage(language)
    }

    fun setCustomFontEnabled(enabled: Boolean) {
        preferencesManager.setCustomFontEnabled(enabled)
    }
}
