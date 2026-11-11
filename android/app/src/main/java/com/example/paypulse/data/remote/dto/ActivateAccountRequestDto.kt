package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
class ActivateAccountRequestDto(
    val token: String,
    val password: String,
    val confirmPassword: String
)
