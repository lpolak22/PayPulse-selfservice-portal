package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TerminalRefDto(
    val id: Long,
    val name: String
)
