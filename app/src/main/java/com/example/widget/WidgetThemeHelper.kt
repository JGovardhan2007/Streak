package com.example.widget

import android.content.Context
import android.graphics.Color

object WidgetThemeHelper {

    fun getAccentColorHex(context: Context): Int {
        val prefs = context.getSharedPreferences("streak_squad_prefs", Context.MODE_PRIVATE)
        val accentName = prefs.getString("theme_accent", "NEON_GREEN") ?: "NEON_GREEN"
        return when (accentName) {
            "DEEP_PURPLE" -> Color.parseColor("#A855F7")
            "SUNSET_ORANGE" -> Color.parseColor("#FF6B00")
            "CYBER_CYAN" -> Color.parseColor("#00E5FF")
            "ELECTRIC_PINK" -> Color.parseColor("#FF2A85")
            else -> Color.parseColor("#13EC5B") // NEON_GREEN
        }
    }

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences("streak_squad_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_dark_mode", true)
    }

    fun getBackgroundColor(context: Context): Int {
        return if (isDarkMode(context)) Color.parseColor("#16181F") else Color.parseColor("#F3F4F6")
    }

    fun getTextColor(context: Context): Int {
        return if (isDarkMode(context)) Color.parseColor("#FFFFFF") else Color.parseColor("#111827")
    }

    fun getSubtextColor(context: Context): Int {
        return if (isDarkMode(context)) Color.parseColor("#9CA3AF") else Color.parseColor("#4B5563")
    }
}
