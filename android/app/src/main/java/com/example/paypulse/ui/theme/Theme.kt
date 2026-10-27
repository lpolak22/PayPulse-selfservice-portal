package com.example.paypulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Red600,
    onPrimary = Color.White,
    primaryContainer = Red50,
    onPrimaryContainer = Red900,
    secondaryContainer = Grey100,
    onSecondaryContainer = Grey900,
    background = Color.White,
    onBackground = Grey900,
    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey700,
    surfaceContainerHighest = Grey100,
    outline = Grey600,
    outlineVariant = Grey300
)

private val DarkColorScheme = darkColorScheme(
    primary = Red300,
    onPrimary = Grey900
)

@Composable
fun PayPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}