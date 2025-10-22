package com.evening.dailylife.feature.me

import android.os.Build
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Palette
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.designsystem.component.ItemPopup
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState

@OptIn(UnstableSaltApi::class)
@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val profileStatsState by viewModel.profileStatsState.collectAsState()
    val themeModePopupMenuState = rememberPopupState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val context = LocalContext.current
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)

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
                                    style = MaterialTheme.typography.labelSmall,
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
