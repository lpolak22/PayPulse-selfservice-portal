package com.example.paypulse.domain.terminal

interface TerminalRepository {
    suspend fun search(query: TerminalQuery): TerminalResult
}
