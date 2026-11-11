package com.example.paypulse.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordResetLinkTest {

    @Test
    fun validLink_returnsToken() {
        assertEquals("abc.def-ghi_123", passwordResetTokenFrom("paypulse://reset-password?token=abc.def-ghi_123"))
    }

    @Test
    fun encodedToken_isDecoded() {
        assertEquals("a+b", passwordResetTokenFrom("paypulse://reset-password?token=a%2Bb"))
    }

    @Test
    fun missingOrBlankToken_returnsNull() {
        assertNull(passwordResetTokenFrom("paypulse://reset-password"))
        assertNull(passwordResetTokenFrom("paypulse://reset-password?token="))
        assertNull(passwordResetTokenFrom("paypulse://reset-password?token=%20"))
    }

    @Test
    fun activationLink_isNotAResetLinkAndTheOtherWayAround() {
        assertNull(passwordResetTokenFrom("paypulse://activate?token=abc"))
        assertNull(activationTokenFrom("paypulse://reset-password?token=abc"))
    }

    @Test
    fun noOrMalformedLink_returnsNull() {
        assertNull(passwordResetTokenFrom(null))
        assertNull(passwordResetTokenFrom("not a link"))
        assertNull(passwordResetTokenFrom("paypulse://reset-password?token=%zz"))
    }
}
