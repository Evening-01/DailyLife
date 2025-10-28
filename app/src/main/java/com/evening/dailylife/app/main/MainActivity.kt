package com.evening.dailylife.app.main

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.DailyTheme
import com.evening.dailylife.core.data.preferences.PreferencesKeys
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.security.biometric.BiometricLockManager
import com.moriafly.salt.ui.UnstableSaltApi
import dagger.hilt.android.AndroidEntryPoint
import io.fastkv.FastKV
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var biometricLockManager: BiometricLockManager

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldUseDynamicSplashIcon()) {
            setTheme(R.style.Theme_App_Starting_Dynamic)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricLockManager.register(this)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()
            val textSizeOption by viewModel.textSizeOption.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()
            val customFontEnabled by viewModel.customFontEnabled.collectAsState()

            LaunchedEffect(appLanguage) {
                val desiredTag = appLanguage.languageTag
                val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                if (currentTag != desiredTag) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(desiredTag)
                    )
                }
            }

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            DailyTheme(
                dynamicColor = dynamicColor,
                darkTheme = darkTheme,
                textSizeScale = textSizeOption.scale,
                useCustomFont = customFontEnabled
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
