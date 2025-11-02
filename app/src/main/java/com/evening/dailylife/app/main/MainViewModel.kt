package com.evening.dailylife.app.main

import androidx.lifecycle.ViewModel
import com.evening.dailylife.core.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class MainViewModel @Inject constructor(
    // 只注入，不需要 private val
    preferencesManager: PreferencesManager
) : ViewModel() {

    // 直接暴露来自 PreferencesManager 的 StateFlow
    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor
    val uiScale = preferencesManager.uiScale
    val fontScale = preferencesManager.fontScale
    val customFontEnabled = preferencesManager.customFontEnabled

    private val _navigationRequests = MutableSharedFlow<NavigationCommand>(extraBufferCapacity = 1)
    val navigationRequests: SharedFlow<NavigationCommand> = _navigationRequests.asSharedFlow()

    fun dispatchNavigation(command: NavigationCommand) {
        _navigationRequests.tryEmit(command)
    }

    data class NavigationCommand(
        val route: String,
        val clearBackStack: Boolean = false
    )
}
