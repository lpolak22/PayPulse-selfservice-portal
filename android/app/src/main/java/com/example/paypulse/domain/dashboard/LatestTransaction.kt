package com.example.paypulse.domain.dashboard

import java.math.BigDecimal
import java.time.Instant

data class LatestTransaction(
    val cardBrand: CardBrand,
    val maskedPan: String?,
    val outletName: String,
    val transactionAt: Instant,
    val amount: BigDecimal,
    val status: TransactionStatus
)
