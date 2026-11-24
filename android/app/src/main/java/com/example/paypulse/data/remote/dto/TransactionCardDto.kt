package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionCardDto(
    val brand: String? = null,
    val maskedPan: String? = null
)
