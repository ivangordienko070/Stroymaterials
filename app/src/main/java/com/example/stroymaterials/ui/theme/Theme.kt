// ui/theme/Theme.kt (минимальная версия)
package com.example.stroymaterials.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6FDBAA),
    secondary = Color(0xFFB3CCBD),
    tertiary = Color(0xFFA5CDDE)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4A),
    secondary = Color(0xFF4D6357),
    tertiary = Color(0xFF3D6473)
)

@Composable
fun StroymaterialsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        "system" -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}