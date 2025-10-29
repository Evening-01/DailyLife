package com.evening.dailylife.feature.me.settings.general

import androidx.lifecycle.ViewModel
import com.evening.dailylife.core.data.preferences.AppLanguage
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val dynamicColor = preferencesManager.dynamicColor
    val themeMode = preferencesManager.themeMode
    val uiScale = preferencesManager.uiScale
    val fontScale = preferencesManager.fontScale
    val appLanguage = preferencesManager.appLanguage
    val customFontEnabled = preferencesManager.customFontEnabled

    private val _pendingLanguage = MutableStateFlow<AppLanguage?>(null)
    val pendingLanguage: StateFlow<AppLanguage?> = _pendingLanguage.asStateFlow()

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun previewFontScale(scale: Float) {
        preferencesManager.setUiScale(1.0f, persist = false)
        preferencesManager.setFontScale(scale, persist = false)
    }

    fun confirmFontScale(fontScale: Float) {
        preferencesManager.setUiScale(1.0f, persist = true)
        preferencesManager.setFontScale(fontScale, persist = true)
    }

    fun revertFontScale(fontScale: Float) {
        preferencesManager.setUiScale(1.0f, persist = false)
        preferencesManager.setFontScale(fontScale, persist = false)
    }

    fun resetScaleToDefault() {
        preferencesManager.setUiScale(1.0f, persist = false)
        previewFontScale(1.0f)
    }

    fun onLanguageOptionSelected(language: AppLanguage) {
        if (language != appLanguage.value) {
            _pendingLanguage.value = language
        }
    }

    fun confirmLanguageChange() {
        val target = _pendingLanguage.value ?: return
        preferencesManager.setAppLanguage(target)
        _pendingLanguage.value = null
    }

    fun dismissLanguageChange() {
        _pendingLanguage.value = null
    }

    fun setCustomFontEnabled(enabled: Boolean) {
        preferencesManager.setCustomFontEnabled(enabled)
    }
}
