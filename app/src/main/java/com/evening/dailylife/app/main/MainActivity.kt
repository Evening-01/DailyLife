package com.evening.dailylife.app.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.evening.dailylife.R
import com.evening.dailylife.app.main.root.MainRoot
import com.evening.dailylife.app.main.splash.shouldUseDynamicSplashIcon
import com.evening.dailylife.app.main.viewmodel.MainViewModel
import com.evening.dailylife.core.domain.language.LanguageUseCase
import com.evening.dailylife.core.security.biometric.BiometricLockManager
import com.evening.dailylife.core.common.readPersistedLanguageCode
import com.evening.dailylife.core.common.wrapContextWithLanguage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldUseDynamicSplashIcon(applicationContext)) {
            setTheme(R.style.Theme_App_Starting_Dynamic)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        biometricLockManager.register(this)
        viewModel.handleNavigationIntent(intent)
        setContent {
            MainRoot(
                viewModel = viewModel,
                biometricLockManager = biometricLockManager,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleNavigationIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (languageUseCase.getPersistedLanguageCode().isNotBlank()) {
            languageUseCase.reapplyPersistedLanguage()
        }
    }

}
