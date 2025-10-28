package com.evening.dailylife.feature.me.settings.general

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.designsystem.component.ItemPopup
import com.evening.dailylife.core.navigation.debouncedPopBackStack
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun GeneralSettingsScreen(
    navController: NavHostController,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
) {
    val dynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val themeModePopupState = rememberPopupState()
    val context = LocalContext.current
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.debouncedPopBackStack() },
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
                popupWidth = 140,
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

    }
}
