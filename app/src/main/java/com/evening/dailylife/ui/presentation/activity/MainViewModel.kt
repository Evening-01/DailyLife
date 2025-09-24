package com.evening.dailylife.ui.presentation.activity

import androidx.lifecycle.ViewModel
import com.evening.dailylife.data.preferences.PreferencesManager
import com.evening.dailylife.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _themeMode = MutableStateFlow(preferencesManager.themeMode)
    val themeMode = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(preferencesManager.dynamicColor)
    val dynamicColor = _dynamicColor.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.themeMode = themeMode
        _themeMode.value = themeMode
    }

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.dynamicColor = enabled
        _dynamicColor.value = enabled
    }
}