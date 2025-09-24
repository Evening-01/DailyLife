package com.evening.dailylife.ui.screens.me

import androidx.lifecycle.ViewModel
import com.evening.dailylife.data.preferences.PreferencesManager
import com.evening.dailylife.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // 直接暴露状态
    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor

    // setter 直接调用 PreferencesManager 的方法
    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }
}