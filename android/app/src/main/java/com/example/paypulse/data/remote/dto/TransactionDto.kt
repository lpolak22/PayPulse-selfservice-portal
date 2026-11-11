package com.example.paypulse.data.remote.dto

import com.example.paypulse.data.remote.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class TransactionDto(
    val id: Long,
    val transactionAt: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val currency: String? = null,
    val status: String? = null,
    val type: String? = null,
    val cardBrand: String? = null,
    val maskedPan: String? = null,
    val salesChannel: String? = null,
    val outletName: String? = null,
    val terminalId: String? = null,
    val authCode: String? = null,
    val rrn: String? = null
)
