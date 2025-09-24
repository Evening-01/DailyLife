package com.evening.dailylife.ui.screens.me

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.data.preferences.ThemeMode
import com.evening.dailylife.ui.component.ItemPopup
import com.evening.dailylife.ui.component.RYScaffold
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
    viewModel: MeViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeModePopupMenuState = rememberPopupState()

    RYScaffold(
        title = "我的",
        navController = null,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.user_interface))

                    // 动态颜色开关项
                    // 注意：动态颜色仅在 Android 12 (API 31) 及以上版本可用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ItemSwitcher(
                            state = isDynamicColorEnabled,
                            onChange = { checked ->
                                viewModel.setDynamicColor(checked)
                            },
                            text = stringResource(R.string.dynamic_color_switcher_text),
                            sub = stringResource(R.string.dynamic_color_switcher_sub),
                            // IMPORTANT: Create a drawable named 'color.xml'
                            iconPainter = painterResource(id = R.drawable.ic_launcher_foreground),
                            iconPaddingValues = PaddingValues(all = 1.7.dp),
                            iconColor = SaltTheme.colors.text,
                        )
                    }


                    // Theme Mode Popup
                    ItemPopup(
                        state = themeModePopupMenuState,
                        // IMPORTANT: Create a drawable named 'app_theme.xml'
                        iconPainter = painterResource(id = R.drawable.ic_launcher_foreground),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.theme_mode_switcher_text),
                        selectedItem = stringResource(id = themeMode.resId),
                        popupWidth = 140
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