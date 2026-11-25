package com.example.paypulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

private const val NON_TEXT_AA = 3f
private const val HIGH_CONTRAST_BORDER = 4.5f

class BorderContrastTest {

    @Test
    fun lightTheme_fieldBordersHaveAtLeast3To1() {
        assertContrast(fieldBorders(payPulseColorScheme(darkTheme = false, highContrast = false)), NON_TEXT_AA)
    }

    @Test
    fun darkTheme_fieldBordersHaveAtLeast3To1() {
        assertContrast(fieldBorders(payPulseColorScheme(darkTheme = true, highContrast = false)), NON_TEXT_AA)
    }

    @Test
    fun highContrastLight_fieldBordersAndDividersHaveAtLeast4_5To1() {
        assertContrast(bordersAndDividers(payPulseColorScheme(darkTheme = false, highContrast = true)), HIGH_CONTRAST_BORDER)
    }

    @Test
    fun highContrastDark_fieldBordersAndDividersHaveAtLeast4_5To1() {
        assertContrast(bordersAndDividers(payPulseColorScheme(darkTheme = true, highContrast = true)), HIGH_CONTRAST_BORDER)
    }

    private fun assertContrast(pairs: List<Triple<String, Color, Color>>, minimum: Float) {
        val failing = pairs
            .map { (name, border, background) -> name to contrastRatio(border, background) }
            .filter { (_, ratio) -> ratio < minimum }
        assertTrue("Contrast below $minimum:1 for $failing", failing.isEmpty())
    }

    private fun fieldBorders(scheme: ColorScheme): List<Triple<String, Color, Color>> = with(scheme) {
        listOf(
            Triple("outline/surface", outline, surface),
            Triple("outline/background", outline, background),
            Triple("onSurfaceVariant/surface", onSurfaceVariant, surface),
            Triple("onSurfaceVariant/surfaceContainerHighest", onSurfaceVariant, surfaceContainerHighest)
        )
    }

    private fun bordersAndDividers(scheme: ColorScheme): List<Triple<String, Color, Color>> = with(scheme) {
        fieldBorders(scheme) + listOf(
            Triple("outlineVariant/surface", outlineVariant, surface),
            Triple("outlineVariant/background", outlineVariant, background)
        )
    }
}
