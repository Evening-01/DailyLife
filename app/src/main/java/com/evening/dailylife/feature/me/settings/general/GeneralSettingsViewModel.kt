package com.evening.dailylife.feature.me.settings.general

import androidx.lifecycle.ViewModel
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val dynamicColor = preferencesManager.dynamicColor
    val themeMode = preferencesManager.themeMode

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }
}
