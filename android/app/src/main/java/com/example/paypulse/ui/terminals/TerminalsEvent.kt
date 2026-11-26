package com.example.paypulse.ui.terminals

import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType

sealed interface TerminalsEvent {
    data object MenuClicked : TerminalsEvent
    data class TerminalClicked(val id: Long) : TerminalsEvent
    data class StatusSelected(val status: TerminalStatus?) : TerminalsEvent
    data class TypeSelected(val type: TerminalType?) : TerminalsEvent
    data object ClearFilters : TerminalsEvent
    data object Retry : TerminalsEvent
    data object Refresh : TerminalsEvent
    data object LoadMore : TerminalsEvent
}
