package com.example.paypulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.paypulse.domain.accessibility.AccessibilitySettings

@Composable
fun PayPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibility: AccessibilitySettings = AccessibilitySettings(),
    content: @Composable () -> Unit
) {
    val systemDensity = LocalDensity.current
    val fontScale = effectiveFontScale(systemDensity.fontScale, accessibility.largerText)
    val density = if (fontScale == systemDensity.fontScale) systemDensity else Density(systemDensity.density, fontScale)
    CompositionLocalProvider(LocalDensity provides density) {
        MaterialTheme(
            colorScheme = payPulseColorScheme(darkTheme, accessibility.highContrast),
            typography = Typography,
            content = content
        )
    }
}
