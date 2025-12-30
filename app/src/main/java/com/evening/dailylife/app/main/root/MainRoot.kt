package com.evening.dailylife.app.main.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.evening.dailylife.core.ui.navigation.Route
import com.evening.dailylife.core.ui.biometric.BiometricOverlayHost
import com.evening.dailylife.app.main.edge.EdgeToEdgeEffect
import com.evening.dailylife.app.main.viewmodel.MainViewModel
import com.evening.dailylife.core.ui.designsystem.theme.DailyTheme
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.security.biometric.BiometricLockManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainRoot(
    viewModel: MainViewModel,
    biometricLockManager: BiometricLockManager,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val uiScale by viewModel.uiScale.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val customFontEnabled by viewModel.customFontEnabled.collectAsState()
    val lockRequired by biometricLockManager.lockRequired.collectAsState()
    val navController = rememberNavController()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    EdgeToEdgeEffect(isDarkTheme = darkTheme)

    DailyTheme(
        dynamicColor = dynamicColor,
        darkTheme = darkTheme,
        uiScale = uiScale,
        fontScale = fontScale,
        useCustomFont = customFontEnabled,
    ) {
        LaunchedEffect(navController) {
            viewModel.navigationRequests.collectLatest { command ->
                if (command.clearBackStack) {
                    navController.popBackStack(
                        route = Route.HOME,
                        inclusive = false,
                        saveState = false,
                    )
                }
                navController.navigate(command.route) {
                    launchSingleTop = true
                }
            }
        }
        BiometricOverlayHost(lockRequired = lockRequired) {
            DailyLifeApp(navController = navController)
        }
    }
}
