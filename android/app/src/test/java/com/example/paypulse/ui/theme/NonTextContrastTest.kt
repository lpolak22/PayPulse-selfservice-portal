package com.example.paypulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

private const val AA_NON_TEXT = 3f
private const val HIGH_CONTRAST_NON_TEXT = 4.5f

class NonTextContrastTest {

    @Test
    fun lightTheme_textFieldBordersHaveAtLeast3To1() {
        assertBorderContrast(payPulseColorScheme(darkTheme = false, highContrast = false), AA_NON_TEXT, includeDividers = false)
    }

    @Test
    fun darkTheme_textFieldBordersHaveAtLeast3To1() {
        assertBorderContrast(payPulseColorScheme(darkTheme = true, highContrast = false), AA_NON_TEXT, includeDividers = false)
    }

    @Test
    fun highContrastLight_bordersAndDividersHaveAtLeast4_5To1() {
        assertBorderContrast(payPulseColorScheme(darkTheme = false, highContrast = true), HIGH_CONTRAST_NON_TEXT, includeDividers = true)
    }

    @Test
    fun highContrastDark_bordersAndDividersHaveAtLeast4_5To1() {
        assertBorderContrast(payPulseColorScheme(darkTheme = true, highContrast = true), HIGH_CONTRAST_NON_TEXT, includeDividers = true)
    }

    private fun assertBorderContrast(scheme: ColorScheme, minimum: Float, includeDividers: Boolean) {
        val failing = borderPairs(scheme, includeDividers)
            .map { (name, border, background) -> name to contrastRatio(border, background) }
            .filter { (_, ratio) -> ratio < minimum }
        assertTrue("Contrast below $minimum:1 for $failing", failing.isEmpty())
    }

    private fun borderPairs(scheme: ColorScheme, includeDividers: Boolean): List<Triple<String, Color, Color>> = with(scheme) {
        val borders = listOf(
            Triple("outline/surface", outline, surface),
            Triple("outline/background", outline, background)
        )
        val dividers = listOf(
            Triple("outlineVariant/surface", outlineVariant, surface),
            Triple("outlineVariant/background", outlineVariant, background)
        )
        if (includeDividers) borders + dividers else borders
    }
}
