package com.evening.dailylife.ui.screens.me

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LineStyle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.data.preferences.ThemeMode
import com.evening.dailylife.ui.component.RYScaffold
import com.google.android.material.color.DynamicColors
import com.moriafly.salt.ui.ItemPopup
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import kotlinx.coroutines.launch

@Composable
fun MeScreen(
    viewModel: MeViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeModePopupMenuState = rememberPopupState()

    RYScaffold(
        title = "我的",
        navController = null // 在主页的顶层，不需要返回按钮
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            item {


                RoundedColumn {
                    ItemTitle(text = stringResource(R.string.user_interface))

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
                        ThemeMode.values().forEach { mode ->
                            PopupMenuItem(
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    themeModePopupMenuState.dismiss()
                                },
                                text = stringResource(id = mode.resId),
                            )
                        }
                    }


                    // 动态颜色开关项
                    // 注意：动态颜色仅在 Android 12 (API 31) 及以上版本可用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsItemSwitch(
                            title = "动态颜色",
                            subtitle = "从壁纸中提取颜色应用到界面",
                            checked = isDynamicColorEnabled,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }

                    // 主题模式选择项
                    SettingsItemPopup(
                        title = "主题模式",
                        currentValue = themeMode,
                        onValueChange = { viewModel.setThemeMode(it) }
                    )


                }
            }
        }
    }
}

// 封装一个带开关的设置项 Composable
@Composable
fun SettingsItemSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// 封装一个带弹出菜单的设置项 Composable
@Composable
fun SettingsItemPopup(
    title: String,
    currentValue: ThemeMode,
    onValueChange: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // 将枚举转换为可读的中文文本
    fun themeModeToText(themeMode: ThemeMode): String {
        return when (themeMode) {
            ThemeMode.SYSTEM -> "跟随系统"
            ThemeMode.LIGHT -> "亮色模式"
            ThemeMode.DARK -> "暗色模式"
        }
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = themeModeToText(currentValue),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.values().forEach { themeMode ->
                DropdownMenuItem(
                    text = { Text(themeModeToText(themeMode)) },
                    onClick = {
                        onValueChange(themeMode)
                        expanded = false
                    }
                )
            }
        }
    }
}