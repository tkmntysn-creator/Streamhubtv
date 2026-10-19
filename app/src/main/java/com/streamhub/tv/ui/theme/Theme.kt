package com.streamhub.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.streamhub.tv.data.local.ThemeMode

private val DarkColors = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryVariant,
    secondary = CyanAccent,
    tertiary = PinkAccent,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = Color(0xFFB8C0D0),
    onPrimaryContainer = Color.White,
    error = ErrorRed
)

private val LightColors = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryVariant,
    secondary = CyanAccent,
    tertiary = PinkAccent,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = Color(0xFF5F6878),
    onPrimaryContainer = Color.White,
    error = ErrorRed
)

/**
 * App-wide Material 3 theme. Dark mode is the default per the product spec; users may
 * switch to Light or System from Settings.
 */
@Composable
fun StreamHubTVTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreamHubTypography,
        shapes = StreamHubShapes,
        content = content
    )
}
