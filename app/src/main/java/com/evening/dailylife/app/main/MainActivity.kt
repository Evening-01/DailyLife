package com.evening.dailylife.app.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.evening.dailylife.R
import com.evening.dailylife.app.navigation.Route
import com.evening.dailylife.app.ui.theme.DailyTheme
import com.evening.dailylife.core.data.preferences.PreferencesKeys
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.domain.language.LanguageUseCase
import com.evening.dailylife.core.security.biometric.BiometricLockManager
import com.evening.dailylife.core.util.readPersistedLanguageCode
import com.evening.dailylife.core.util.wrapContextWithLanguage
import com.moriafly.salt.ui.UnstableSaltApi
import dagger.hilt.android.AndroidEntryPoint
import io.fastkv.FastKV
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var biometricLockManager: BiometricLockManager
    @Inject lateinit var languageUseCase: LanguageUseCase

    override fun attachBaseContext(newBase: Context) {
        val languageCode = readPersistedLanguageCode(newBase)
        val wrapped = wrapContextWithLanguage(newBase, languageCode)
        super.attachBaseContext(wrapped)
    }

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldUseDynamicSplashIcon()) {
            setTheme(R.style.Theme_App_Starting_Dynamic)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        biometricLockManager.register(this)
        handleNavigationIntent(intent)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()
            val uiScale by viewModel.uiScale.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()
            val customFontEnabled by viewModel.customFontEnabled.collectAsState()
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
                useCustomFont = customFontEnabled
            ) {
                LaunchedEffect(navController) {
                    viewModel.navigationRequests.collectLatest { command ->
                        if (command.clearBackStack) {
                            navController.popBackStack(
                                route = Route.HOME,
                                inclusive = false,
                                saveState = false
                            )
                        }
                        navController.navigate(command.route) {
                            launchSingleTop = true
                        }
                    }
                }
                DailyLifeApp(navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (languageUseCase.getPersistedLanguageCode().isNotBlank()) {
            languageUseCase.reapplyPersistedLanguage()
        }
    }

    private fun shouldUseDynamicSplashIcon(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false
        }
        val fastKV = FastKV.Builder(applicationContext, PreferencesKeys.PREFERENCES_NAME).build()
        return fastKV.getBoolean(PreferencesKeys.KEY_DYNAMIC_COLOR, false)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return
        val rawRoute = intent.getStringExtra(EXTRA_NAVIGATE_ROUTE) ?: return
        val categoryId = intent.getStringExtra(EXTRA_WIDGET_CATEGORY_ID)?.takeIf { it.isNotBlank() }
        val isExpenseProvided = intent.hasExtra(EXTRA_WIDGET_IS_EXPENSE)
        val isExpense = if (isExpenseProvided) {
            intent.getBooleanExtra(EXTRA_WIDGET_IS_EXPENSE, true)
        } else {
            null
        }
        val targetRoute = if (rawRoute.startsWith("add_edit_transaction")) {
            Route.addNewTransactionShortcut(categoryId, isExpense)
        } else {
            rawRoute
        }
        viewModel.dispatchNavigation(
            MainViewModel.NavigationCommand(route = targetRoute)
        )
        intent.removeExtra(EXTRA_NAVIGATE_ROUTE)
        intent.removeExtra(EXTRA_WIDGET_IS_EXPENSE)
        intent.removeExtra(EXTRA_WIDGET_CATEGORY_ID)
    }

    companion object {
        const val EXTRA_NAVIGATE_ROUTE = "extra_navigate_route"
        const val EXTRA_WIDGET_IS_EXPENSE = "extra_widget_is_expense"
        const val EXTRA_WIDGET_CATEGORY_ID = "extra_widget_category_id"
    }
}
