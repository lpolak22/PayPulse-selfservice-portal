package com.example.paypulse.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {

    @Test
    fun largerTextOff_keepsSystemScale() {
        assertEquals(1.5f, effectiveFontScale(1.5f, largerText = false), 0.001f)
    }

    @Test
    fun largerTextOn_atSystem100_gives130() {
        assertEquals(1.3f, effectiveFontScale(1.0f, largerText = true), 0.001f)
    }

    @Test
    fun largerTextOn_atSystem150_gives195() {
        assertEquals(1.95f, effectiveFontScale(1.5f, largerText = true), 0.001f)
    }

    @Test
    fun largerTextOn_atSystem200_staysAt200() {
        assertEquals(2.0f, effectiveFontScale(2.0f, largerText = true), 0.001f)
    }

    @Test
    fun largerTextOn_neverGoesAbove200() {
        assertEquals(2.0f, effectiveFontScale(1.6f, largerText = true), 0.001f)
    }
}
