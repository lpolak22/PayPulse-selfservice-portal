package com.example.paypulse.data.remote.dto

import com.example.paypulse.data.remote.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class TransactionDetailsDto(
    val id: Long,
    val transactionAt: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val currency: String? = null,
    val status: String? = null,
    val type: String? = null,
    val salesChannel: String? = null,
    val authCode: String? = null,
    val rrn: String? = null,
    val acquirer: String? = null,
    val card: TransactionCardDto,
    val terminal: TransactionTerminalDto,
    val outlet: TransactionOutletDto,
    val merchant: TransactionMerchantDto
)
