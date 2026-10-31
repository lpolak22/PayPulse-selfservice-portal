package com.example.paypulse.data.remote

data class ApiError(
    val code: String,
    val fieldNames: List<String>
)
