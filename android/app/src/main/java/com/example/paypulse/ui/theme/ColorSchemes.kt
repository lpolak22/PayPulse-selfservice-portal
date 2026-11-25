package com.example.paypulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Red600,
    onPrimary = Color.White,
    primaryContainer = Red50,
    onPrimaryContainer = Red900,
    secondary = Orange700,
    onSecondary = Color.White,
    secondaryContainer = Grey100,
    onSecondaryContainer = Grey900,
    tertiary = Green700,
    onTertiary = Color.White,
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
    onPrimary = Grey900,
    primaryContainer = Red950,
    onPrimaryContainer = Red50,
    secondary = Orange300,
    onSecondary = Grey900,
    tertiary = Green300,
    onTertiary = Grey900
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Red900,
    onPrimary = Color.White,
    primaryContainer = Red50,
    onPrimaryContainer = Red950,
    secondary = Orange900,
    onSecondary = Color.White,
    secondaryContainer = Grey100,
    onSecondaryContainer = Color.Black,
    tertiary = Green900,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey900,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Grey100,
    surfaceContainerHigh = Grey100,
    surfaceContainerHighest = Grey100,
    outline = Grey900,
    outlineVariant = Grey700,
    error = Red900,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red950
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Red200,
    onPrimary = Color.Black,
    primaryContainer = Red950,
    onPrimaryContainer = Color.White,
    secondary = Orange200,
    onSecondary = Color.Black,
    secondaryContainer = Grey850,
    onSecondaryContainer = Color.White,
    tertiary = Green200,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Grey850,
    onSurfaceVariant = Grey100,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Grey850,
    surfaceContainerHigh = Grey850,
    surfaceContainerHighest = Grey850,
    outline = Grey100,
    outlineVariant = Grey400,
    error = Red200,
    onError = Color.Black,
    errorContainer = Red950,
    onErrorContainer = Color.White
)

fun payPulseColorScheme(darkTheme: Boolean, highContrast: Boolean): ColorScheme = when {
    darkTheme && highContrast -> HighContrastDarkColorScheme
    highContrast -> HighContrastLightColorScheme
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
