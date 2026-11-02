package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAccentColor = staticCompositionLocalOf { Color(0xFF13EC5B) }

@Composable
fun StreakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentTheme: AppThemeAccent = AppThemeAccent.NEON_GREEN,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = accentTheme.primaryColor,
        onPrimary = accentTheme.onPrimaryColor,
        primaryContainer = accentTheme.primaryContainerDark,
        onPrimaryContainer = accentTheme.primaryColor,
        secondary = accentTheme.darkAccentColor,
        onSecondary = Color.White,
        background = DarkBackground,
        onBackground = DarkText,
        surface = DarkSurface,
        onSurface = DarkText,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkSubtext,
        outline = Color(0xFF2E323D)
    )

    val lightColorScheme = lightColorScheme(
        primary = accentTheme.lightPrimaryColor,
        onPrimary = Color.White,
        primaryContainer = accentTheme.primaryContainerLight,
        onPrimaryContainer = accentTheme.lightPrimaryColor,
        secondary = accentTheme.primaryColor,
        onSecondary = accentTheme.onPrimaryColor,
        background = LightBackground,
        onBackground = LightText,
        surface = LightSurface,
        onSurface = LightText,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightSubtext,
        outline = Color(0xFFD0D7D1)
    )

    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    CompositionLocalProvider(LocalAccentColor provides accentTheme.primaryColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


