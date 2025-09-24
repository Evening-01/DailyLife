package com.evening.dailylife.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs

// 更新后的亮色主题方案
private val LightColorScheme = lightColorScheme(
    primary = SunnyGold,
    onPrimary = OnSunnyGold,
    primaryContainer = SunnyGoldContainer,
    onPrimaryContainer = OnSunnyGoldContainer,
    inversePrimary = WarmGinger,

    secondary = SoftCitrine,
    onSecondary = OnSoftCitrine,
    secondaryContainer = SunnyGoldContainer,      // 恢复为原来的配色
    onSecondaryContainer = OnSunnyGoldContainer, // 恢复为原来的配色

    tertiary = MutedSage,
    onTertiary = OnMutedSage,
    tertiaryContainer = MutedSageContainer,
    onTertiaryContainer = OnMutedSageContainer,

    background = ParchmentWhite,
    onBackground = TextPrimaryLight,

    surface = ParchmentWhite, // 与 background 保持一致
    onSurface = TextPrimaryLight,
    surfaceVariant = ParchmentWhite,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = SunnyGold,
    inverseSurface = DeepSlate,
    inverseOnSurface = TextPrimaryDark,

    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFF141213),

    outline = Color(0xFFD3CFC4),
    outlineVariant = Color(0xFFEAE6DA),
    scrim = Color(0x99000000),
)

// 更新后的暗色主题方案
private val DarkColorScheme = darkColorScheme(
    primary = WarmGinger,
    onPrimary = OnWarmGinger,
    primaryContainer = WarmGingerContainer,
    onPrimaryContainer = OnWarmGingerContainer,
    inversePrimary = SunnyGold,

    secondary = PaleMoon,
    onSecondary = OnPaleMoon,
    secondaryContainer = WarmGingerContainer,       // 恢复为原来的配色
    onSecondaryContainer = OnWarmGingerContainer,  // 恢复为原来的配色

    tertiary = SageNight,
    onTertiary = OnSageNight,
    tertiaryContainer = SageNightContainer,
    onTertiaryContainer = OnSageNightContainer,

    background = DeepSlate,
    onBackground = TextPrimaryDark,

    surface = DeepSlate, // 与 background 保持一致
    onSurface = TextPrimaryDark,
    surfaceVariant = DeepSlate,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = WarmGinger,
    inverseSurface = ParchmentWhite,
    inverseOnSurface = TextPrimaryLight,

    error = Color(0xFFCF6679),
    onError = Color(0xFF141414),
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFCD8DF),

    outline = Color(0xFF9F9A8F),
    outlineVariant = Color(0xFF4F4A42),
    scrim = Color(0x99000000),
)


@SuppressLint("NewApi")
@OptIn(UnstableSaltApi::class)
@Composable
fun DailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val materialColorScheme = remember(darkTheme, dynamicColor) {
        val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        when {
            useDynamicColor -> {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            else -> {
                if (darkTheme) DarkColorScheme
                else LightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    SaltTheme(
        colors = saltColorsByColorScheme(materialColorScheme),
        configs = saltConfigs(isDarkTheme = darkTheme)
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}