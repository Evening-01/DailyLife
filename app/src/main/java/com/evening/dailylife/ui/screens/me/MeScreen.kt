package com.evening.dailylife.ui.screens.me

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.evening.dailylife.R
import com.evening.dailylife.data.preferences.ThemeMode
import com.evening.dailylife.ui.component.ItemPopup
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState

@OptIn(UnstableSaltApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeModePopupMenuState = rememberPopupState()

    val context = LocalContext.current
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)


    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.me),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding)
        ) {

            item {
                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.user_interface))

                    // 动态颜色切换
                    Box {
                        // ItemSwitcher 正常显示，并根据系统版本决定其可用状态
                        ItemSwitcher(
                            state = isDynamicColorEnabled,
                            onChange = { checked ->
                                viewModel.setDynamicColor(checked)
                            },
                            enabled = isDynamicColorSupported, // 根据系统版本决定开关是否可用
                            text = stringResource(R.string.dynamic_color_switcher_text),
                            sub = stringResource(R.string.dynamic_color_switcher_sub),
                            iconPainter = rememberVectorPainter(image = Icons.Outlined.Palette),
                            iconPaddingValues = PaddingValues(all = 1.8.dp),
                            iconColor = SaltTheme.colors.text,
                        )

                        // 覆盖一个透明的、可点击的遮罩层
                        if (!isDynamicColorSupported) {
                            Spacer(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            Toast
                                                .makeText(context, dynamicColorUnsupportedMessage, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    )
                            )
                        }
                    }

                    // 主题模式切换
                    ItemPopup(
                        state = themeModePopupMenuState,
                        iconPainter = rememberVectorPainter(image = Icons.Outlined.BrightnessMedium),
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