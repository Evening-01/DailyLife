package com.evening.dailylife.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs

// 为导航栏/表面定义的专属颜色
private val SurfaceLight = Color(0xFFFAF6ED) // 柔和燕麦色，用于亮色主题的表面，增加区分度
private val SurfaceDark = Color(0xFF3C3831)  // 较亮的炭灰色，用于暗色主题的表面，避免过于深沉

// 更新后的亮色主题方案
private val LightColorScheme = lightColorScheme(
    primary = SunnyGold,
    onPrimary = OnSunnyGold,
    primaryContainer = SunnyGoldContainer,
    onPrimaryContainer = OnSunnyGoldContainer,

    secondary = SoftCitrine,
    onSecondary = OnSoftCitrine,
    secondaryContainer = SunnyGoldContainer,      // 使用主色调容器作为选择色，保证色调统一
    onSecondaryContainer = OnSunnyGoldContainer, // 确保选择色上的文字清晰

    tertiary = MutedSage,
    onTertiary = OnMutedSage,
    tertiaryContainer = MutedSageContainer,
    onTertiaryContainer = OnMutedSageContainer,

    background = ParchmentWhite,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight, // 使用新的表面颜色
    onSurface = TextPrimaryLight,
    surfaceVariant = MutedSageContainer,
    onSurfaceVariant = TextSecondaryLight,

    error = Color(0xFFB00020),
    onError = Color.White
)

// 更新后的暗色主题方案
private val DarkColorScheme = darkColorScheme(
    primary = WarmGinger,
    onPrimary = OnWarmGinger,
    primaryContainer = WarmGingerContainer,
    onPrimaryContainer = OnWarmGingerContainer,

    secondary = PaleMoon,
    onSecondary = OnPaleMoon,
    secondaryContainer = WarmGingerContainer,       // 使用主色调容器作为选择色
    onSecondaryContainer = OnWarmGingerContainer,  // 确保选择色上的文字清晰

    tertiary = SageNight,
    onTertiary = OnSageNight,
    tertiaryContainer = SageNightContainer,
    onTertiaryContainer = OnSageNightContainer,

    background = DeepSlate,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark, // 使用新的表面颜色
    onSurface = TextPrimaryDark,
    surfaceVariant = SageNightContainer,
    onSurfaceVariant = TextSecondaryDark,

    error = Color(0xFFCF6679),
    onError = Color(0xFF141414)
)


@SuppressLint("NewApi")
@OptIn(UnstableSaltApi::class)
@Composable
fun DailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // 参数默认值为 true
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val materialColorScheme = remember(darkTheme, dynamicColor) {
        val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        when {
            // 如果开启了动态颜色且系统支持 (Android 12+)
            useDynamicColor -> {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            // 如果关闭了动态颜色或系统不支持
            else -> {
                if (darkTheme) DarkColorScheme // 使用自定义的暗色主题
                else LightColorScheme // 使用自定义的亮色主题
            }
        }
    }

    SaltTheme(
        colors = saltColorsByColorScheme(materialColorScheme),
        configs = saltConfigs(isDarkTheme = darkTheme)
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography, // 确保 Typography 已定义
            content = content
        )
    }
}

