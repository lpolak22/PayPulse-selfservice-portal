package com.example.paypulse.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastRatioTest {

    @Test
    fun blackOnWhite_isTheMaximumOf21() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
    }

    @Test
    fun sameColors_isTheMinimumOf1() {
        assertEquals(1f, contrastRatio(Color(0xFF767676), Color(0xFF767676)), 0.001f)
    }

    @Test
    fun swappingTextAndBackground_givesTheSameRatio() {
        val darkOnLight = contrastRatio(Color(0xFFD92D35), Color.White)
        val lightOnDark = contrastRatio(Color.White, Color(0xFFD92D35))
        assertEquals(darkOnLight, lightOnDark, 0.0001f)
    }

    @Test
    fun grey767676OnWhite_justPassesAa() {
        val ratio = contrastRatio(Color(0xFF767676), Color.White)
        assertEquals(4.54f, ratio, 0.01f)
        assertTrue(ratio >= 4.5f)
    }

    @Test
    fun grey777777OnWhite_justFailsAa() {
        val ratio = contrastRatio(Color(0xFF777777), Color.White)
        assertEquals(4.48f, ratio, 0.01f)
        assertTrue(ratio < 4.5f)
    }

    @Test
    fun grey595959OnWhite_justPassesAaa() {
        val ratio = contrastRatio(Color(0xFF595959), Color.White)
        assertEquals(7.00f, ratio, 0.01f)
        assertTrue(ratio >= 7f)
    }

    @Test
    fun brandRedOnWhite_is4_8() {
        assertEquals(4.80f, contrastRatio(Color(0xFFD92D35), Color.White), 0.01f)
    }

    @Test
    fun veryDarkGreyOnBlack_usesTheLinearPartOfTheSrgbCurve() {
        assertEquals(1.06f, contrastRatio(Color(0xFF0A0A0A), Color.Black), 0.01f)
    }
}
