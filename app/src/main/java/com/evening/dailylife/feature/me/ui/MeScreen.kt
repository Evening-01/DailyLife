package com.evening.dailylife.feature.me.ui

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.BuildConfig
import com.evening.dailylife.R
import com.evening.dailylife.core.ui.biometric.BiometricOverlayController
import com.evening.dailylife.core.ui.biometric.LocalBiometricOverlayController
import com.evening.dailylife.core.ui.designsystem.theme.LocalExtendedColorScheme
import com.evening.dailylife.core.common.launchExternalUrl
import com.evening.dailylife.feature.me.ui.component.MeInterfaceSettingsSection
import com.evening.dailylife.feature.me.ui.component.MeOtherSection
import com.evening.dailylife.feature.me.ui.component.MeProfileHeader
import com.evening.dailylife.feature.me.ui.component.MeSecuritySection
import com.moriafly.salt.ui.UnstableSaltApi
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel(),
    onAboutAuthorClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onQuickUsageClick: () -> Unit,
    onDataManagementClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
) {
    val profileStatsState by viewModel.profileStatsState.collectAsState()
    val fingerprintLockEnabled by viewModel.fingerprintLockEnabled.collectAsState()

    val context = LocalContext.current
    val fingerprintUnsupportedMessage = stringResource(R.string.fingerprint_not_supported)
    val fingerprintNotEnrolledMessage = stringResource(R.string.fingerprint_not_enrolled)
    val biometricManager = remember { BiometricManager.from(context) }
    val authenticator = BiometricManager.Authenticators.BIOMETRIC_STRONG
    val fingerprintCapability = biometricManager.canAuthenticate(authenticator)
    val isFingerprintSupported = fingerprintCapability == BiometricManager.BIOMETRIC_SUCCESS ||
        fingerprintCapability == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    val overlayController = LocalBiometricOverlayController.current
    val promptInfo = remember(context) { buildFingerprintPromptInfo(context) }

    val shareSubject = stringResource(R.string.me_share_app)
    val shareApp = remember(shareSubject, context) {
        createShareAppAction(context = context, shareSubject = shareSubject)
    }

    val openSourceRepo: () -> Unit = {
        context.launchExternalUrl(DAILY_LIFE_REPOSITORY_URL)
    }

    fun handleFingerprintToggle(checked: Boolean) {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            overlayController.setVisible(false)
            Toast.makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT).show()
            if (!checked) {
                viewModel.setFingerprintLockEnabled(true)
            }
            return
        }

        when (biometricManager.canAuthenticate(authenticator)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                launchFingerprintPrompt(
                    activity = activity,
                    promptInfo = promptInfo,
                    overlayController = overlayController,
                    onSuccess = { viewModel.setFingerprintLockEnabled(checked) },
                    onError = { errorCode, errString ->
                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_TIMEOUT
                        ) {
                            Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                        }
                        if (!checked) {
                            viewModel.setFingerprintLockEnabled(true)
                        }
                    },
                )
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                overlayController.setVisible(false)
                Toast.makeText(context, fingerprintNotEnrolledMessage, Toast.LENGTH_SHORT).show()
                if (!checked) {
                    viewModel.setFingerprintLockEnabled(true)
                }
            }

            else -> {
                overlayController.setVisible(false)
                Toast.makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT).show()
                if (!checked) {
                    viewModel.setFingerprintLockEnabled(true)
                }
            }
        }
    }

    MeScreenContent(
        profileStatsState = profileStatsState,
        fingerprintLockEnabled = fingerprintLockEnabled,
        isFingerprintSupported = isFingerprintSupported,
        onFingerprintToggle = ::handleFingerprintToggle,
        onFingerprintUnsupported = {
            Toast
                .makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT)
                .show()
        },
        onDataManagementClick = onDataManagementClick,
        onGeneralSettingsClick = onGeneralSettingsClick,
        onQuickUsageClick = onQuickUsageClick,
        onAboutAuthorClick = onAboutAuthorClick,
        onShareAppClick = shareApp,
        onMoreInfoClick = onMoreInfoClick,
        onOpenSourceClick = openSourceRepo,
    )
}

@Composable
private fun MeScreenContent(
    profileStatsState: MeProfileStatsUiState,
    fingerprintLockEnabled: Boolean,
    isFingerprintSupported: Boolean,
    onFingerprintToggle: (Boolean) -> Unit,
    onFingerprintUnsupported: () -> Unit,
    onDataManagementClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onQuickUsageClick: () -> Unit,
    onAboutAuthorClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    onOpenSourceClick: () -> Unit,
) {
    val extendedColors = LocalExtendedColorScheme.current
    val headerContainerColor = extendedColors.headerContainer
    val headerContentColor = extendedColors.onHeaderContainer

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
                    onFingerprintToggle = onFingerprintToggle,
                    onFingerprintUnsupported = onFingerprintUnsupported,
                    onDataManagementClick = onDataManagementClick,
                )
            }

            item {
                MeOtherSection(
                    onAboutAuthorClick = onAboutAuthorClick,
                    onShareAppClick = onShareAppClick,
                    onMoreInfoClick = onMoreInfoClick,
                    onOpenSourceClick = onOpenSourceClick,
                )
            }
        }
    }
}

private fun buildFingerprintPromptInfo(
    context: Context,
): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.fingerprint_prompt_title))
        .setSubtitle(context.getString(R.string.fingerprint_prompt_subtitle))
        .setDescription(context.getString(R.string.fingerprint_prompt_description))
        .setNegativeButtonText(context.getString(R.string.fingerprint_prompt_negative))
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
}

private fun launchFingerprintPrompt(
    activity: FragmentActivity,
    promptInfo: BiometricPrompt.PromptInfo,
    overlayController: BiometricOverlayController,
    onSuccess: () -> Unit,
    onError: (Int, CharSequence) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                overlayController.setVisible(false)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                overlayController.setVisible(false)
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                // Keep waiting for a valid fingerprint input.
            }
        },
    )
    overlayController.setVisible(true)
    prompt.authenticate(promptInfo)
}

private fun createShareAppAction(
    context: Context,
    shareSubject: String,
): () -> Unit = {
    runCatching {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
        val sourceApk = File(applicationInfo.sourceDir)
        val sharedDir = File(context.cacheDir, "shared_apk").apply { mkdirs() }
        val sanitizedName = context.getString(R.string.app_name)
            .replace("\\s+".toRegex(), "_")
        val cacheApk = File(sharedDir, "$sanitizedName-${BuildConfig.VERSION_NAME}.apk")

        val needsCopy = !cacheApk.exists() || cacheApk.length() != sourceApk.length()
        if (needsCopy) {
            sourceApk.inputStream().use { input ->
                cacheApk.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            cacheApk,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.me_share_app_message))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, shareSubject, apkUri)
        }
        val chooser = Intent.createChooser(intent, shareSubject)
        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }.onFailure {
        if (it !is CancellationException) {
            Toast
                .makeText(context, context.getString(R.string.me_share_app_unavailable), Toast.LENGTH_SHORT)
                .show()
        }
    }
}

private const val DAILY_LIFE_REPOSITORY_URL = "https://github.com/Evening-01/DailyLife"

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
