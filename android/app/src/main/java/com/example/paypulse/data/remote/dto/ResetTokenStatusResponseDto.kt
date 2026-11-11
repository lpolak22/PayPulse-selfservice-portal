package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetTokenStatusResponseDto(
    val status: String
)
