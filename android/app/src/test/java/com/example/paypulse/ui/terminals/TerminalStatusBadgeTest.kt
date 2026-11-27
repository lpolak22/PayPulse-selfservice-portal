package com.example.paypulse.ui.terminals

import com.example.paypulse.R
import com.example.paypulse.domain.terminal.TerminalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TerminalStatusBadgeTest {

    private fun badgeFor(status: TerminalStatus) = terminalBadge(status, status.name)

    @Test
    fun active_isPositiveAktivan() {
        assertEquals(TerminalBadge(BadgeTone.POSITIVE, label = R.string.terminals_status_active), badgeFor(TerminalStatus.ACTIVE))
    }

    @Test
    fun inactiveAndDeactivated_areNeutral() {
        assertEquals(TerminalBadge(BadgeTone.NEUTRAL, label = R.string.terminals_status_inactive), badgeFor(TerminalStatus.INACTIVE))
        assertEquals(
            TerminalBadge(BadgeTone.NEUTRAL, label = R.string.terminals_status_deactivated),
            badgeFor(TerminalStatus.DEACTIVATED)
        )
    }

    @Test
    fun suspendedAndBlocked_areNegative() {
        assertEquals(BadgeTone.NEGATIVE, badgeFor(TerminalStatus.SUSPENDED).tone)
        assertEquals(BadgeTone.NEGATIVE, badgeFor(TerminalStatus.BLOCKED).tone)
    }

    @Test
    fun preparedAndAuthenticated_arePending() {
        assertEquals(BadgeTone.PENDING, badgeFor(TerminalStatus.PREPARED).tone)
        assertEquals(BadgeTone.PENDING, badgeFor(TerminalStatus.AUTHENTICATED).tone)
    }

    @Test
    fun everyKnownStatus_hasLabel() {
        TerminalStatus.entries.forEach { status ->
            val badge = badgeFor(status)
            assertNotNull(status.name, badge.label)
            assertNull(status.name, badge.rawText)
        }
    }

    @Test
    fun unknownApiValue_showsRawTextNeutral() {
        assertEquals(TerminalBadge(BadgeTone.NEUTRAL, rawText = "RETIRED"), terminalBadge(null, "RETIRED"))
    }

    @Test
    fun blankApiValue_showsNepoznat() {
        assertEquals(TerminalBadge(BadgeTone.NEUTRAL, label = R.string.terminals_status_unknown), terminalBadge(null, " "))
    }
}
