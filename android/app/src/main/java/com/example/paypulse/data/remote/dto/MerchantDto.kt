package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MerchantDto(
    val id: Long,
    val merchantId: String? = null,
    val name: String,
    val registrationNumber: String,
    val status: String? = null,
    val outletCount: Long = 0,
    val terminalCount: Long = 0
)
