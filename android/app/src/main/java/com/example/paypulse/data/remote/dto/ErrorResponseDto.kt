package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val code: String,
    val fieldErrors: List<FieldErrorDto> = emptyList()
)
