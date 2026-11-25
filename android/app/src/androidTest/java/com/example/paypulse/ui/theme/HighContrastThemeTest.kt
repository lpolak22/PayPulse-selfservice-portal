package com.example.paypulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighContrastThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun schemeUsedBy(darkTheme: Boolean, highContrast: Boolean): ColorScheme {
        lateinit var scheme: ColorScheme
        composeRule.setContent {
            PayPulseTheme(darkTheme = darkTheme, accessibility = AccessibilitySettings(highContrast = highContrast)) {
                scheme = MaterialTheme.colorScheme
            }
        }
        composeRule.waitForIdle()
        return scheme
    }

    @Test
    fun systemDarkTheme_usesDarkColorsWithAtLeastAaContrast() {
        val scheme = schemeUsedBy(darkTheme = true, highContrast = false)
        assertTrue(isDark(scheme.background))
        assertTrue(contrastRatio(scheme.onBackground, scheme.background) >= 4.5f)
    }

    @Test
    fun systemDarkThemeWithHighContrast_usesDarkColorsWithAtLeastAaaContrast() {
        val scheme = schemeUsedBy(darkTheme = true, highContrast = true)
        assertTrue(isDark(scheme.background))
        assertTrue(contrastRatio(scheme.onBackground, scheme.background) >= 7f)
        assertTrue(contrastRatio(scheme.onSurfaceVariant, scheme.surface) >= 7f)
    }

    @Test
    fun systemLightThemeWithHighContrast_usesLightColorsWithAtLeastAaaContrast() {
        val scheme = schemeUsedBy(darkTheme = false, highContrast = true)
        assertTrue(!isDark(scheme.background))
        assertTrue(contrastRatio(scheme.onBackground, scheme.background) >= 7f)
        assertTrue(contrastRatio(scheme.onSurfaceVariant, scheme.surface) >= 7f)
    }

    private fun isDark(color: Color): Boolean = contrastRatio(color, Color.White) > contrastRatio(color, Color.Black)
}
