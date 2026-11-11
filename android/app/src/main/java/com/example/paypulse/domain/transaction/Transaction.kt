package com.example.paypulse.domain.transaction

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: Long,
    val transactionAt: Instant,
    val amount: BigDecimal,
    val currency: String?,
    val status: TransactionStatus?,
    val type: TransactionType,
    val cardBrand: CardBrand,
    val maskedPan: String?,
    val salesChannel: SalesChannel?,
    val outletName: String?,
    val terminalId: String?,
    val authCode: String?,
    val rrn: String?
)
