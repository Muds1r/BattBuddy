package com.batterycalc.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    secondary = Color(0xFF5C5C5C),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF6B6B6B),
    outline = Color(0xFFE8E8E8),
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = Color(0xFF1A1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF121212),
    secondary = Color(0xFFB0B0B0),
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF9A9A9A),
    outline = Color(0xFF2E2E2E),
    primaryContainer = Color(0xFF242424),
    onPrimaryContainer = Color(0xFFF2F2F2)
)

@Composable
fun BatteryCalcTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
