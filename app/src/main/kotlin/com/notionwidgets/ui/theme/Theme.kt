package com.notionwidgets.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8E8E8),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF505050),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF505050),
    error = Color(0xFFEB5757),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2D2D2D),
    onPrimaryContainer = Color(0xFFE8E8E8),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF191919),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF191919),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFEB5757),
    onError = Color(0xFF000000)
)

@Composable
fun NotionWidgetsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
