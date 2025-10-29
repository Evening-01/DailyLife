package com.evening.dailylife.feature.me.settings.general

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.AppLanguage
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.designsystem.component.ItemPopup
import com.evening.dailylife.core.navigation.safePopBackStack
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun GeneralSettingsScreen(
    navController: NavHostController,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
) {
    val dynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val customFontEnabled by viewModel.customFontEnabled.collectAsState()
    val pendingLanguage by viewModel.pendingLanguage.collectAsState()

    val themeModePopupState = rememberPopupState()
    val scalePopupState = rememberPopupState()
    val languagePopupState = rememberPopupState()
    val context = LocalContext.current
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)

    var scalePopupHandled by remember { mutableStateOf(true) }
    var initialFontScale by remember { mutableStateOf(fontScale) }

    LaunchedEffect(scalePopupState.expend) {
        if (scalePopupState.expend) {
            scalePopupHandled = false
            initialFontScale = fontScale
        } else if (!scalePopupHandled) {
            viewModel.revertFontScale(initialFontScale)
            scalePopupHandled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.general_settings),
        )

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.user_interface))

            Box {
                ItemSwitcher(
                    state = dynamicColorEnabled,
                    onChange = viewModel::setDynamicColor,
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
                                    .makeText(
                                        context,
                                        dynamicColorUnsupportedMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                    )
                }
            }

            ItemPopup(
                state = themeModePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.BrightnessMedium),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.theme_mode_switcher_text),
                selectedItem = stringResource(id = themeMode.resId),
                popupWidth = 160,
            ) {
                ThemeMode.entries.forEach { mode ->
                    PopupMenuItem(
                        onClick = {
                            viewModel.setThemeMode(mode)
                            themeModePopupState.dismiss()
                        },
                        text = stringResource(id = mode.resId),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.text_language_settings))

            val scaleSummary = stringResource(
                R.string.text_scale_selected_percent,
                (fontScale * 100).roundToInt(),
            )

            ItemPopup(
                state = scalePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.FormatSize),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.text_scale_switcher_text),
                sub = stringResource(R.string.text_scale_switcher_sub),
                selectedItem = scaleSummary,
                popupWidth = 240,
            ) {
                ScalePopupContent(
                    fontScale = fontScale,
                    onFontScaleChange = viewModel::previewFontScale,
                    onReset = {
                        viewModel.resetScaleToDefault()
                    },
                    onConfirm = { confirmedFont ->
                        viewModel.confirmFontScale(confirmedFont)
                        scalePopupHandled = true
                        scalePopupState.dismiss()
                    },
                    onCancel = {
                        viewModel.revertFontScale(initialFontScale)
                        scalePopupHandled = true
                        scalePopupState.dismiss()
                    }
                )
            }

            ItemPopup(
                state = languagePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Language),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.language_switcher_text),
                sub = stringResource(R.string.language_switcher_sub),
                selectedItem = stringResource(id = appLanguage.resId),
                popupWidth = 200,
            ) {
                AppLanguage.entries.forEach { language ->
                    PopupMenuItem(
                        onClick = {
                            viewModel.onLanguageOptionSelected(language)
                            languagePopupState.dismiss()
                        },
                        text = stringResource(id = language.resId),
                    )
                }
            }

            ItemSwitcher(
                state = customFontEnabled,
                onChange = viewModel::setCustomFontEnabled,
                text = stringResource(R.string.custom_font_switcher_text),
                sub = stringResource(R.string.custom_font_switcher_sub),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.FontDownload),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
            )
        }
    }

    pendingLanguage?.let { target ->
        LanguageRestartDialog(
            targetLanguage = target,
            onConfirm = viewModel::confirmLanguageChange,
            onDismiss = viewModel::dismissLanguageChange,
        )
    }
}

@Composable
private fun ScalePopupContent(
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    onReset: () -> Unit,
    onConfirm: (Float) -> Unit,
    onCancel: () -> Unit,
) {
    var fontScaleState by remember(fontScale) { mutableStateOf(fontScale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.text_scale_popup_title),
            style = SaltTheme.textStyles.main,
            color = SaltTheme.colors.text,
        )

        ScaleSlider(
            title = stringResource(R.string.font_scale_label),
            value = fontScaleState,
            onValueChange = {
                fontScaleState = it
                onFontScaleChange(it)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                fontScaleState = 1.0f
                onReset()
            }) {
                Text(text = stringResource(R.string.action_reset))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.common_cancel))
                }
                Button(onClick = { onConfirm(fontScaleState) }) {
                    Text(text = stringResource(R.string.common_confirm))
                }
            }
        }
    }
}

@Composable
private fun ScaleSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = SaltTheme.textStyles.main,
            color = SaltTheme.colors.text,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.scale_value_percent, (value * 100).roundToInt()),
                style = SaltTheme.textStyles.sub,
                color = SaltTheme.colors.subText,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0.9f..1.2f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LanguageRestartDialog(
    targetLanguage: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.language_restart_title))
        },
        text = {
            val targetLabel = stringResource(id = targetLanguage.resId)
            Text(text = stringResource(R.string.language_restart_message, targetLabel))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.restart_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}
