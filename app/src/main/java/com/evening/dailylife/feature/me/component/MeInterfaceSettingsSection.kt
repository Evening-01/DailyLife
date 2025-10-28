package com.evening.dailylife.feature.me.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@OptIn(UnstableSaltApi::class)
@Composable
fun MeInterfaceSettingsSection(
    dynamicColorEnabled: Boolean,
    isDynamicColorSupported: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onDynamicColorUnsupported: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onGeneralSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeModePopupMenuState = rememberPopupState()

    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(R.string.user_interface))

        Box {
            ItemSwitcher(
                state = dynamicColorEnabled,
                onChange = { checked ->
                    onDynamicColorChange(checked)
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
                            onDynamicColorUnsupported()
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
                        onThemeModeSelected(mode)
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
            onClick = onGeneralSettingsClick,
        )
    }
}
