package com.evening.dailylife.app.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.PreferencesKeys
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.designsystem.theme.DailyTheme
import com.moriafly.salt.ui.UnstableSaltApi
import dagger.hilt.android.AndroidEntryPoint
import io.fastkv.FastKV

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldUseDynamicSplashIcon()) {
            setTheme(R.style.Theme_App_Starting_Dynamic)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            DailyTheme(
                dynamicColor = dynamicColor,
                darkTheme = darkTheme
            ) {
                DailyLifeApp()
            }
        }
    }
    private fun shouldUseDynamicSplashIcon(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false
        }
        val fastKV = FastKV.Builder(applicationContext, PreferencesKeys.PREFERENCES_NAME).build()
        return fastKV.getBoolean(PreferencesKeys.KEY_DYNAMIC_COLOR, false)
    }
}