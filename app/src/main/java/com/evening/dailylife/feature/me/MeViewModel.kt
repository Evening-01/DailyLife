package com.evening.dailylife.feature.me

import androidx.lifecycle.ViewModel
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }
}
