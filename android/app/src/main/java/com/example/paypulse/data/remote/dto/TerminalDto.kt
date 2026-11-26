package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TerminalDto(
    val id: Long,
    val terminalId: String,
    val type: String,
    val status: String,
    val outlet: TerminalRefDto,
    val merchant: TerminalRefDto
)
