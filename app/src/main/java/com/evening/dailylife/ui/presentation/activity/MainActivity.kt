package com.evening.dailylife.ui.presentation.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.evening.dailylife.data.preferences.ThemeMode
import com.evening.dailylife.ui.presentation.common.HomeEntry
import com.evening.dailylife.ui.theme.DailyTheme
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.lightSaltColors
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                else -> false
            }
            val colors = when (themeMode) {
                ThemeMode.LIGHT -> if (dynamicColor) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    saltColorsByColorScheme(
                        dynamicLightColorScheme(this)
                    )
                } else {
                    TODO("VERSION.SDK_INT < S")
                } else lightSaltColors()

                ThemeMode.DARK -> if (dynamicColor) saltColorsByColorScheme(dynamicDarkColorScheme(this)) else darkSaltColors()

                ThemeMode.SYSTEM -> {
                    if (isSystemInDarkTheme())
                        if (dynamicColor) saltColorsByColorScheme(
                            dynamicDarkColorScheme(this)
                        ) else darkSaltColors()
                    else
                        if (dynamicColor) saltColorsByColorScheme(
                            dynamicLightColorScheme(this)
                        ) else lightSaltColors()
                }
            }
            SaltTheme(
                colors = colors,
                configs = saltConfigs(darkTheme)
            ) {
                HomeEntry()
            }
        }
    }
}