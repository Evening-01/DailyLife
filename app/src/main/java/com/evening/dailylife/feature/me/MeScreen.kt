package com.evening.dailylife.feature.me

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.feature.me.component.MeInterfaceSettingsSection
import com.evening.dailylife.feature.me.component.MeOtherSection
import com.evening.dailylife.feature.me.component.MeProfileHeader
import com.evening.dailylife.feature.me.component.MeSecuritySection
import com.moriafly.salt.ui.UnstableSaltApi
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel(),
    onAboutAuthorClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onQuickUsageClick: () -> Unit,
) {
    val profileStatsState by viewModel.profileStatsState.collectAsState()
    val fingerprintLockEnabled by viewModel.fingerprintLockEnabled.collectAsState()

    val extendedColors = LocalExtendedColorScheme.current
    val headerContainerColor = extendedColors.headerContainer
    val headerContentColor = extendedColors.onHeaderContainer

    val context = LocalContext.current
    val fingerprintUnsupportedMessage = stringResource(R.string.fingerprint_not_supported)
    val fingerprintNotEnrolledMessage = stringResource(R.string.fingerprint_not_enrolled)
    val biometricManager = remember { BiometricManager.from(context) }
    val fingerprintCapability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    val isFingerprintSupported = fingerprintCapability == BiometricManager.BIOMETRIC_SUCCESS ||
        fingerprintCapability == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

    val shareSubject = stringResource(R.string.me_share_app)
    val shareMessage = stringResource(R.string.me_share_app_message)

    val shareApp: () -> Unit = {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            val chooser = Intent.createChooser(intent, shareSubject)
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.onFailure {
            Toast
                .makeText(context, context.getString(R.string.me_share_app_unavailable), Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun handleFingerprintToggle(checked: Boolean) {
        if (checked) {
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    viewModel.setFingerprintLockEnabled(true)
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(context, fingerprintNotEnrolledMessage, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val activity = context.findFragmentActivity()
            if (activity == null) {
                Toast.makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT).show()
                viewModel.setFingerprintLockEnabled(true)
                return
            }

            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val executor = ContextCompat.getMainExecutor(activity)
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(context.getString(R.string.fingerprint_prompt_title))
                        .setSubtitle(context.getString(R.string.fingerprint_prompt_subtitle))
                        .setDescription(context.getString(R.string.fingerprint_prompt_description))
                        .setNegativeButtonText(context.getString(R.string.fingerprint_prompt_negative))
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        .build()
                    val prompt = BiometricPrompt(
                        activity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                viewModel.setFingerprintLockEnabled(false)
                            }

                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                    errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                    errorCode != BiometricPrompt.ERROR_TIMEOUT
                                ) {
                                    Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                                }
                                viewModel.setFingerprintLockEnabled(true)
                            }

                            override fun onAuthenticationFailed() {
                                // 等待下一次输入，不立即改变状态
                            }
                        },
                    )
                    prompt.authenticate(promptInfo)
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(context, fingerprintNotEnrolledMessage, Toast.LENGTH_SHORT).show()
                    viewModel.setFingerprintLockEnabled(true)
                }

                else -> {
                    Toast.makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT).show()
                    viewModel.setFingerprintLockEnabled(true)
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MeProfileHeader(
                    stats = profileStatsState,
                    containerColor = headerContainerColor,
                    contentColor = headerContentColor,
                )
            }

            item {
                MeInterfaceSettingsSection(
                    onGeneralSettingsClick = onGeneralSettingsClick,
                    onQuickUsageClick = onQuickUsageClick,
                )
            }

            item {
                MeSecuritySection(
                    fingerprintLockEnabled = fingerprintLockEnabled,
                    isFingerprintSupported = isFingerprintSupported,
                    onFingerprintToggle = { checked -> handleFingerprintToggle(checked) },
                    onFingerprintUnsupported = {
                        Toast
                            .makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT)
                            .show()
                    },
                    onDataManagementClick = {
                    },
                )
            }

            item {
                MeOtherSection(
                    onAboutAuthorClick = onAboutAuthorClick,
                    onShareAppClick = shareApp,
                    onMoreInfoClick = {
                        Toast
                            .makeText(context, R.string.me_more_info, Toast.LENGTH_SHORT)
                            .show()
                    },
                )
            }
        }
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
