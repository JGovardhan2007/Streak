package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF13EC5B)
val NeonGreenDark = Color(0xFF0EC248)

// Theme Accent Options
enum class AppThemeAccent(
    val displayName: String,
    val primaryColor: Color,
    val darkAccentColor: Color,
    val lightPrimaryColor: Color,
    val onPrimaryColor: Color,
    val primaryContainerDark: Color,
    val primaryContainerLight: Color,
    val emoji: String
) {
    NEON_GREEN(
        displayName = "Neon Green",
        primaryColor = Color(0xFF13EC5B),
        darkAccentColor = Color(0xFF0EC248),
        lightPrimaryColor = Color(0xFF0C8A36),
        onPrimaryColor = Color(0xFF003913),
        primaryContainerDark = Color(0xFF133F25),
        primaryContainerLight = Color(0xFFDCFCE7),
        emoji = "🟢"
    ),
    DEEP_PURPLE(
        displayName = "Deep Purple",
        primaryColor = Color(0xFFA855F7),
        darkAccentColor = Color(0xFF9333EA),
        lightPrimaryColor = Color(0xFF7E22CE),
        onPrimaryColor = Color(0xFFFFFFFF),
        primaryContainerDark = Color(0xFF3B0764),
        primaryContainerLight = Color(0xFFF3E8FF),
        emoji = "🟣"
    ),
    SUNSET_ORANGE(
        displayName = "Sunset Orange",
        primaryColor = Color(0xFFFF6B00),
        darkAccentColor = Color(0xFFEA580C),
        lightPrimaryColor = Color(0xFFC2410C),
        onPrimaryColor = Color(0xFFFFFFFF),
        primaryContainerDark = Color(0xFF431407),
        primaryContainerLight = Color(0xFFFFEDD5),
        emoji = "🟠"
    ),
    CYBER_CYAN(
        displayName = "Cyber Cyan",
        primaryColor = Color(0xFF00E5FF),
        darkAccentColor = Color(0xFF00B8D4),
        lightPrimaryColor = Color(0xFF00838F),
        onPrimaryColor = Color(0xFF00363A),
        primaryContainerDark = Color(0xFF00363A),
        primaryContainerLight = Color(0xFFE0F7FA),
        emoji = "🔵"
    ),
    ELECTRIC_PINK(
        displayName = "Electric Pink",
        primaryColor = Color(0xFFFF2A85),
        darkAccentColor = Color(0xFFE00065),
        lightPrimaryColor = Color(0xFFB3004B),
        onPrimaryColor = Color(0xFFFFFFFF),
        primaryContainerDark = Color(0xFF4A001F),
        primaryContainerLight = Color(0xFFFFE4E6),
        emoji = "🩷"
    )
}

// Light Palette
val LightBackground = Color(0xFFF6F8F6)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAEFEA)
val LightText = Color(0xFF111813)
val LightSubtext = Color(0xFF556358)
val LightPrimaryContainer = Color(0xFFDCFCE7)

// Dark Palette
val DarkBackground = Color(0xFF0D0E12)
val DarkSurface = Color(0xFF16181F)
val DarkSurfaceVariant = Color(0xFF21242E)
val DarkText = Color(0xFFF0F2F5)
val DarkSubtext = Color(0xFF9CA3AF)
val DarkPrimaryContainer = Color(0xFF1A1D27)

// Danger / Alert
val StreakOrange = Color(0xFFFF6B00)
val DangerRed = Color(0xFFFF3B30)
val WarningYellow = Color(0xFFFFCC00)


