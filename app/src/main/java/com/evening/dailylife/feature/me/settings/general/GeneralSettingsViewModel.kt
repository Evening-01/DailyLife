package com.evening.dailylife.feature.me.settings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.evening.dailylife.core.data.preferences.AppLanguage
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.domain.app.AppActionsUseCase
import com.evening.dailylife.core.domain.language.LanguageUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val languageUseCase: LanguageUseCase,
    private val appActionsUseCase: AppActionsUseCase,
) : ViewModel() {

    val dynamicColor = preferencesManager.dynamicColor
    val themeMode = preferencesManager.themeMode
    val uiScale = preferencesManager.uiScale
    val fontScale = preferencesManager.fontScale
    val customFontEnabled = preferencesManager.customFontEnabled

    val appLanguage: StateFlow<AppLanguage> = languageUseCase.observeLanguageCode()
        .map { code -> AppLanguage.fromPersistedCode(code) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.SYSTEM,
        )

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
        viewModelScope.launch {
            languageUseCase.setLanguage(target.persistedCode)
            _pendingLanguage.value = null
            appActionsUseCase.restart()
        }
    }

    fun dismissLanguageChange() {
        _pendingLanguage.value = null
    }

    fun setCustomFontEnabled(enabled: Boolean) {
        preferencesManager.setCustomFontEnabled(enabled)
    }
}
