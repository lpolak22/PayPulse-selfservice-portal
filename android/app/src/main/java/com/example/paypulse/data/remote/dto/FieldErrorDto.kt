package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldErrorDto(
    val field: String
)
