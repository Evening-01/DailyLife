package com.evening.dailylife.ui.screens.main

import androidx.lifecycle.ViewModel
import com.evening.dailylife.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // 只注入，不需要 private val
    preferencesManager: PreferencesManager
) : ViewModel() {

    // 直接暴露来自 PreferencesManager 的 StateFlow
    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor

}