package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
class PasswordResetRequestDto(
    val email: String
)
