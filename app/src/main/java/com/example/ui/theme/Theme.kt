package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.services.DatabaseService

private fun resolveAccentColors(accentName: String): Pair<Color, Color> {
    return when (accentName) {
        "Cyan Accent", "Cyan" -> Pair(Color(0xFF06B6D4), Color(0xFF0EA5E9))
        "Emerald Green", "Green" -> Pair(Color(0xFF10B981), Color(0xFF059669))
        "Sunset Amber", "Amber", "Gold" -> Pair(Color(0xFFF59E0B), Color(0xFFD97706))
        "Rose Pink", "Pink" -> Pair(Color(0xFFEC4899), Color(0xFFF43F5E))
        "Ocean Blue", "Blue" -> Pair(Color(0xFF3B82F6), Color(0xFF2563EB))
        else -> Pair(Color(0xFF8B5CF6), Color(0xFFA855F7)) // Royal Purple
    }
}

@Composable
fun VedraTheme(
    dbService: DatabaseService? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Reading settingsVersion triggers recomposition whenever any setting changes
    val settingsVer = dbService?.settingsVersion?.intValue ?: 0

    val appTheme = dbService?.getSetting("pref_app_theme", "Dark") ?: "Dark"
    val accentName = dbService?.getSetting("pref_accent_color", "Royal Purple") ?: "Royal Purple"
    val isAmoled = dbService?.getSetting("pref_amoled_dark", "false") == "true"
    val useDynamicColor = dbService?.getSetting("pref_dynamic_color", "true") == "true"

    val isDark = when (appTheme) {
        "Light" -> false
        "Dark" -> true
        "System Default" -> isSystemInDarkTheme()
        else -> true
    }

    val (primaryColor, secondaryColor) = resolveAccentColors(accentName)

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> {
            if (isAmoled) {
                darkColorScheme(
                    primary = primaryColor,
                    secondary = secondaryColor,
                    tertiary = VedraCyanAccent,
                    background = Color(0xFF000000),
                    surface = Color(0xFF0D0E15),
                    surfaceVariant = Color(0xFF151824),
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                    onSurfaceVariant = Color(0xFF9CA3AF),
                    outline = Color(0xFF2A2E47)
                )
            } else {
                darkColorScheme(
                    primary = primaryColor,
                    secondary = secondaryColor,
                    tertiary = VedraCyanAccent,
                    background = Color(0xFF090A10),
                    surface = Color(0xFF121524),
                    surfaceVariant = Color(0xFF1B1F33),
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFF3F4F6),
                    onSurface = Color(0xFFF3F4F6),
                    onSurfaceVariant = Color(0xFF9CA3AF),
                    outline = Color(0xFF2A2E47)
                )
            }
        }
        else -> {
            lightColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = VedraCyanAccent,
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFF1F5F9),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

