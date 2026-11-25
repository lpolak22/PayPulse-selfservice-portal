package com.example.paypulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

private const val AA_TEXT = 4.5f
private const val AAA_TEXT = 7f

class PayPulseColorSchemeTest {

    @Test
    fun lightTheme_everyTextColorHasAtLeastAaContrast() {
        assertTextContrast(payPulseColorScheme(darkTheme = false, highContrast = false), AA_TEXT)
    }

    @Test
    fun darkTheme_everyTextColorHasAtLeastAaContrast() {
        assertTextContrast(payPulseColorScheme(darkTheme = true, highContrast = false), AA_TEXT)
    }

    @Test
    fun highContrastLight_everyTextColorHasAtLeastAaaContrast() {
        assertTextContrast(payPulseColorScheme(darkTheme = false, highContrast = true), AAA_TEXT)
    }

    @Test
    fun highContrastDark_everyTextColorHasAtLeastAaaContrast() {
        assertTextContrast(payPulseColorScheme(darkTheme = true, highContrast = true), AAA_TEXT)
    }

    @Test
    fun highContrastLight_staysLight() {
        val background = payPulseColorScheme(darkTheme = false, highContrast = true).background
        assertTrue(contrastRatio(background, Color.Black) > contrastRatio(background, Color.White))
    }

    @Test
    fun highContrastDark_staysDark() {
        val background = payPulseColorScheme(darkTheme = true, highContrast = true).background
        assertTrue(contrastRatio(background, Color.White) > contrastRatio(background, Color.Black))
    }

    private fun assertTextContrast(scheme: ColorScheme, minimum: Float) {
        val failing = textPairs(scheme)
            .map { (name, text, background) -> name to contrastRatio(text, background) }
            .filter { (_, ratio) -> ratio < minimum }
        assertTrue("Contrast below $minimum:1 for $failing", failing.isEmpty())
    }

    private fun textPairs(scheme: ColorScheme): List<Triple<String, Color, Color>> = with(scheme) {
        listOf(
            Triple("onPrimary/primary", onPrimary, primary),
            Triple("onPrimaryContainer/primaryContainer", onPrimaryContainer, primaryContainer),
            Triple("onSecondary/secondary", onSecondary, secondary),
            Triple("onSecondaryContainer/secondaryContainer", onSecondaryContainer, secondaryContainer),
            Triple("onTertiary/tertiary", onTertiary, tertiary),
            Triple("onBackground/background", onBackground, background),
            Triple("onSurface/surface", onSurface, surface),
            Triple("onSurfaceVariant/surface", onSurfaceVariant, surface),
            Triple("onSurfaceVariant/surfaceVariant", onSurfaceVariant, surfaceVariant),
            Triple("onSurfaceVariant/background", onSurfaceVariant, background),
            Triple("onError/error", onError, error),
            Triple("onErrorContainer/errorContainer", onErrorContainer, errorContainer),
            Triple("primary/surface", primary, surface),
            Triple("primary/background", primary, background),
            Triple("secondary/surface", secondary, surface),
            Triple("tertiary/surface", tertiary, surface),
            Triple("error/surface", error, surface)
        )
    }
}
