package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionOutletDto(
    val id: Long,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null
)
