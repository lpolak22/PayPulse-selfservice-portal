package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OutletDto(
    val id: Long,
    val outletId: String? = null,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val status: String? = null,
    val terminalCount: Long = 0
)
