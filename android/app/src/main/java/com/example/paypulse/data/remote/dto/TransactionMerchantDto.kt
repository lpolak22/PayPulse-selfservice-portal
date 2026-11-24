package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionMerchantDto(
    val id: Long,
    val name: String? = null
)
