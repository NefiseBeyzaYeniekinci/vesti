package com.vesti.app.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VestiColors.Primary,
    secondary = VestiColors.Accent,
    tertiary = VestiColors.SuccessMood,
    background = VestiColors.DarkIndigo,
    surface = VestiColors.DarkIndigo,
    onPrimary = VestiColors.Background,
    onSecondary = VestiColors.Background,
    onTertiary = VestiColors.TextMain,
    onBackground = VestiColors.Background,
    onSurface = VestiColors.Background,
)

private val LightColorScheme = lightColorScheme(
    primary = VestiColors.Primary,
    secondary = VestiColors.Accent,
    tertiary = VestiColors.SuccessMood,
    background = VestiColors.Background,
    surface = VestiColors.Background,
    onPrimary = VestiColors.Background,
    onSecondary = VestiColors.Background,
    onTertiary = VestiColors.TextMain,
    onBackground = VestiColors.TextMain,
    onSurface = VestiColors.TextMain,
)

@Composable
fun VestiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Dynamic color is disabled to use brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
