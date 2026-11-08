package com.example.paypulse.data.remote

data class ApiError(
    val code: String,
    val fieldNames: List<String>,
    val remainingAttempts: Int? = null,
    val retryAfterMinutes: Int? = null
)
