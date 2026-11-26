package com.example.paypulse.domain.terminal

sealed interface TerminalResult {
    data class Success(val page: TerminalPage) : TerminalResult
    data class Failure(val error: TerminalError) : TerminalResult
}
