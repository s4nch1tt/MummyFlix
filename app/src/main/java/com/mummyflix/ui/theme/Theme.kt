package com.mummyflix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8ecae6),
    primaryContainer = Color(0xFF0b4a8b),
    secondary = Color(0xFFf5e625),
    secondaryContainer = Color(0xFFd4be06),
    tertiary = Color(0xFFff6b35),
    background = Color.Black,
    surface = Color(0xFF0b4a8b),
    error = Color(0xFFe63946),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0b4a8b),
    primaryContainer = Color(0xFF8ecae6),
    secondary = Color(0xFFd4be06),
    secondaryContainer = Color(0xFFf5e625),
    tertiary = Color(0xFFff6b35),
    background = Color.White,
    surface = Color(0xFFf0f0f0),
    error = Color(0xFFe63946),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@Composable
fun MummyFlixTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object Typography {
    val bodyLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp
    )
    val bodyMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp
    )
    val labelLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 14.sp
    )
    val headlineMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 20.sp
    )
}