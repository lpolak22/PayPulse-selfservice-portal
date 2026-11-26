package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionTerminalDto(
    val id: Long,
    val terminalId: String? = null,
    val type: String? = null
)
