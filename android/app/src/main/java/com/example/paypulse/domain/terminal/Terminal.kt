package com.example.paypulse.domain.terminal

data class Terminal(
    val id: Long,
    val terminalId: String,
    val type: String,
    val status: TerminalStatus?,
    val rawStatus: String,
    val outletName: String,
    val merchantName: String
)
