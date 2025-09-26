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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs
import com.moriafly.salt.ui.saltTextStyles

// 亮色主题
private val LightColorScheme = lightColorScheme(
    primary = SunnyGold,
    onPrimary = OnSunnyGold,
    primaryContainer = SunnyGoldContainer,
    onPrimaryContainer = OnSunnyGoldContainer,
    inversePrimary = WarmGinger,

    secondary = SoftCitrine,
    onSecondary = OnSoftCitrine,
    secondaryContainer = SunnyGoldContainer,
    onSecondaryContainer = OnSunnyGoldContainer,

    tertiary = MutedSage,
    onTertiary = OnMutedSage,
    tertiaryContainer = MutedSageContainer,
    onTertiaryContainer = OnMutedSageContainer,

    background = ParchmentWhite,
    onBackground = TextPrimaryLight,

    surface = ParchmentWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = ParchmentWhite,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = SunnyGold,
    inverseSurface = DeepSlate,
    inverseOnSurface = TextPrimaryDark,

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Scrim,
)

// 暗色主题
private val DarkColorScheme = darkColorScheme(
    primary = WarmGinger,
    onPrimary = OnWarmGinger,
    primaryContainer = WarmGingerContainer,
    onPrimaryContainer = OnWarmGingerContainer,
    inversePrimary = SunnyGold,

    secondary = PaleMoon,
    onSecondary = OnPaleMoon,
    secondaryContainer = WarmGingerContainer,
    onSecondaryContainer = OnWarmGingerContainer,

    tertiary = SageNight,
    onTertiary = OnSageNight,
    tertiaryContainer = SageNightContainer,
    onTertiaryContainer = OnSageNightContainer,

    background = DeepSlate,
    onBackground = TextPrimaryDark,

    surface = DeepSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = DeepSlate,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = WarmGinger,
    inverseSurface = ParchmentWhite,
    inverseOnSurface = TextPrimaryLight,

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Scrim,
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

    val customSaltTextStyles = saltTextStyles(
        main = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 14.sp, // 对应 MaterialTheme 的 bodyLarge
            fontWeight = FontWeight.Normal
        ),
        sub = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 12.sp, // 对应 MaterialTheme 的 bodyMedium
            fontWeight = FontWeight.Normal
        ),
        paragraph = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp
        )
    )

    SaltTheme(
        colors = saltColorsByColorScheme(materialColorScheme),
        configs = saltConfigs(isDarkTheme = darkTheme),
        textStyles = customSaltTextStyles
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}