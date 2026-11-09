package com.zenlauncher.minimalist.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalLauncherTheme = compositionLocalOf { LauncherThemeConfig() }

@Composable
fun ZenLauncherTheme(
    themeConfig: LauncherThemeConfig = LauncherThemeConfig(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = themeConfig.backgroundColor.toArgb()
            window.navigationBarColor = themeConfig.backgroundColor.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    val dynamicColorScheme = darkColorScheme(
        primary = TextPrimary,
        onPrimary = themeConfig.backgroundColor,
        secondary = TextSecondary,
        onSecondary = themeConfig.backgroundColor,
        tertiary = themeConfig.accentColor,
        background = themeConfig.backgroundColor,
        onBackground = TextPrimary,
        surface = themeConfig.darkSurface,
        onSurface = TextPrimary,
        surfaceVariant = themeConfig.cardSurface,
        onSurfaceVariant = TextSecondary,
        outline = themeConfig.borderSubtle
    )

    CompositionLocalProvider(LocalLauncherTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
