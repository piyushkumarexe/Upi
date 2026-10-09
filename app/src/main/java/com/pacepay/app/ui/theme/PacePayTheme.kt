package com.pacepay.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF174B3B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFCCF38C),
    onSecondary = Color(0xFF183527),
    tertiary = Color(0xFF7770B8),
    background = Color(0xFFF6F5F1),
    onBackground = Color(0xFF18211D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18211D),
    surfaceVariant = Color(0xFFEDECE6),
    onSurfaceVariant = Color(0xFF66726B),
    outline = Color(0xFFE0E2DB),
    error = Color(0xFFB93D35),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDE58B),
    onPrimary = Color(0xFF183527),
    secondary = Color(0xFF9CCB76),
    onSecondary = Color(0xFF183527),
    tertiary = Color(0xFFC9C1FA),
    background = Color(0xFF111814),
    onBackground = Color(0xFFE8EEE9),
    surface = Color(0xFF1B241F),
    onSurface = Color(0xFFE8EEE9),
    surfaceVariant = Color(0xFF27332C),
    onSurfaceVariant = Color(0xFFA3B0A7),
    outline = Color(0xFF35423A),
    error = Color(0xFFFFB4AB),
)

@Composable
fun PacePayTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
