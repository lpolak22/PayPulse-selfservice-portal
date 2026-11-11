package com.example.paypulse.data.remote.dto

import com.example.paypulse.data.remote.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class LatestTransactionDto(
    val cardBrand: String? = null,
    val maskedPan: String? = null,
    val outletName: String,
    val transactionAt: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val status: String
)
