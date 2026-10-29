package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppUserDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val status: String,
    val createdAt: String
)
