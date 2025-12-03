package com.example.freshcookapp.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Cinnabar500,
    onPrimary = Color.White,

    background = Color(0xFF0E100E),
    onBackground = Color(0xFFEDEDED),

    surface = Color(0xFF151715),
    onSurface = Color(0xFFEDEDED),

    surfaceVariant = Color(0xFF202220),
    onSurfaceVariant = Color(0xFFB5B5B5),

    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2C2C2C),

    secondary = Cinnabar500,
    tertiary = Cinnabar500
)


private val LightColorScheme = lightColorScheme(
    primary = Cinnabar500,
    onPrimary = Color.White,

    background = Color(0xFFF9FFF5),
    onBackground = Color(0xFF101110),

    surface = Color.White,
    onSurface = Color(0xFF202020),

    surfaceVariant = Color(0xFFF1F1F1),
    onSurfaceVariant = Color(0xFF6B6B6B),

    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFD0D0D0),

    secondary = Cinnabar500,
    tertiary = Cinnabar500
)

@Composable
fun FreshCookAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        } else {
            if (isDark) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}