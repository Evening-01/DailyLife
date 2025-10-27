package com.evening.dailylife.feature.me

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.designsystem.component.ItemPopup
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val profileStatsState by viewModel.profileStatsState.collectAsState()
    val fingerprintLockEnabled by viewModel.fingerprintLockEnabled.collectAsState()
    val themeModePopupMenuState = rememberPopupState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val context = LocalContext.current
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)
    val fingerprintUnsupportedMessage = stringResource(R.string.fingerprint_not_supported)
    val fingerprintNotEnrolledMessage = stringResource(R.string.fingerprint_not_enrolled)
    val biometricManager = remember { BiometricManager.from(context) }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryColor),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(onPrimaryColor.copy(alpha = 0.12f)),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_user),
                                    contentDescription = stringResource(R.string.me_profile_avatar_content_description),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.me_profile_display_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = onPrimaryColor,
                                )
                                Text(
                                    text = stringResource(R.string.me_profile_signature),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onPrimaryColor.copy(alpha = 0.7f),
                                )
                            }
                        }

                        MeProfileStatsRow(
                            stats = profileStatsState,
                            contentColor = onPrimaryColor,
                        )
                    }
                }
            }

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.user_interface))

                    Box {
                        ItemSwitcher(
                            state = isDynamicColorEnabled,
                            onChange = { checked ->
                                viewModel.setDynamicColor(checked)
                            },
                            enabled = isDynamicColorSupported,
                            text = stringResource(R.string.dynamic_color_switcher_text),
                            sub = stringResource(R.string.dynamic_color_switcher_sub),
                            iconPainter = rememberVectorPainter(image = Icons.Outlined.Palette),
                            iconPaddingValues = PaddingValues(all = 1.8.dp),
                            iconColor = SaltTheme.colors.text,
                        )

                        if (!isDynamicColorSupported) {
                            Spacer(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        Toast
                                            .makeText(context, dynamicColorUnsupportedMessage, Toast.LENGTH_SHORT)
                                            .show()
                                    },
                            )
                        }
                    }

                    ItemPopup(
                        state = themeModePopupMenuState,
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.BrightnessMedium),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.theme_mode_switcher_text),
                        selectedItem = stringResource(id = themeMode.resId),
                        popupWidth = 140,
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            PopupMenuItem(
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    themeModePopupMenuState.dismiss()
                                },
                                text = stringResource(id = mode.resId),
                            )
                        }
                    }

                    Item(
                        text = stringResource(R.string.general_settings),
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.Tune),
                        iconColor = SaltTheme.colors.text,
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        onClick = {
                            // TODO: 在这里处理点击事件，比如跳转到通用设置页面
                        },
                    )
                }
            }

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.data_and_security))

                    ItemSwitcher(
                        state = fingerprintLockEnabled,
                        onChange = { checked ->
                            if (checked) {
                                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                                    BiometricManager.BIOMETRIC_SUCCESS -> {
                                        viewModel.setFingerprintLockEnabled(true)
                                    }

                                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                        Toast
                                            .makeText(context, fingerprintNotEnrolledMessage, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                    else -> {
                                        Toast
                                            .makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            } else {
                                val activity = context.findFragmentActivity()
                                if (activity == null) {
                                    Toast
                                        .makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT)
                                        .show()
                                    viewModel.setFingerprintLockEnabled(true)
                                    return@ItemSwitcher
                                }
                                when (val capability = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
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
                                        Toast
                                            .makeText(context, fingerprintNotEnrolledMessage, Toast.LENGTH_SHORT)
                                            .show()
                                        viewModel.setFingerprintLockEnabled(true)
                                    }

                                    else -> {
                                        Toast
                                            .makeText(context, fingerprintUnsupportedMessage, Toast.LENGTH_SHORT)
                                            .show()
                                        viewModel.setFingerprintLockEnabled(true)
                                    }
                                }
                            }
                        },
                        text = stringResource(R.string.fingerprint_security),
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.Fingerprint),
                        iconColor = SaltTheme.colors.text,
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                    )

                    Item(
                        text = stringResource(R.string.data_management),
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.Dns),
                        iconColor = SaltTheme.colors.text,
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        onClick = {
                        },
                    )

                }
            }
        }
    }
}


@Composable
private fun MeProfileStatsRow(
    stats: MeProfileStatsUiState,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val consecutiveDaysText = if (stats.isLoading) "--" else stats.consecutiveCheckInDays.toString()
    val totalDaysText = if (stats.isLoading) "--" else stats.totalActiveDays.toString()
    val totalTransactionsText = if (stats.isLoading) "--" else stats.totalTransactions.toString()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MeProfileStatItem(
            value = consecutiveDaysText,
            label = stringResource(R.string.me_profile_stat_streak),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalDaysText,
            label = stringResource(R.string.me_profile_stat_total_days),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalTransactionsText,
            label = stringResource(R.string.me_profile_stat_total_transactions),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MeProfileStatItem(
    value: String,
    label: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.7f),
        )
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
