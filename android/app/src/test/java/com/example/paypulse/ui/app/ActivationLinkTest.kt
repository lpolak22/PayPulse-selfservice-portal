package com.example.paypulse.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivationLinkTest {

    @Test
    fun validLink_returnsToken() {
        assertEquals("abc.def-ghi_123", activationTokenFrom("paypulse://activate?token=abc.def-ghi_123"))
    }

    @Test
    fun tokenAmongOtherParameters_isFound() {
        assertEquals("abc", activationTokenFrom("paypulse://activate?lang=hr&token=abc&x=1"))
    }

    @Test
    fun encodedToken_isDecoded() {
        assertEquals("a+b", activationTokenFrom("paypulse://activate?token=a%2Bb"))
    }

    @Test
    fun missingEmptyOrBlankToken_returnsNull() {
        assertNull(activationTokenFrom("paypulse://activate"))
        assertNull(activationTokenFrom("paypulse://activate?token"))
        assertNull(activationTokenFrom("paypulse://activate?token="))
        assertNull(activationTokenFrom("paypulse://activate?token=%20%20"))
    }

    @Test
    fun wrongSchemeOrHost_returnsNull() {
        assertNull(activationTokenFrom("https://activate?token=abc"))
        assertNull(activationTokenFrom("paypulse://login?token=abc"))
    }

    @Test
    fun noLink_returnsNull() {
        assertNull(activationTokenFrom(null))
    }

    @Test
    fun malformedLink_returnsNull() {
        assertNull(activationTokenFrom("paypulse://activate?token=%zz"))
        assertNull(activationTokenFrom("not a link"))
    }
}
