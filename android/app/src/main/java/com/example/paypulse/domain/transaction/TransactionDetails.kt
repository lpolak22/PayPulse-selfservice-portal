package com.example.paypulse.domain.transaction

import com.example.paypulse.domain.dashboard.TransactionStatus
import java.math.BigDecimal
import java.time.Instant

data class TransactionDetails(
    val id: Long,
    val transactionAt: Instant,
    val amount: BigDecimal,
    val currency: String?,
    val status: TransactionStatus?,
    val type: TransactionType,
    val salesChannel: SalesChannel?,
    val authCode: String?,
    val rrn: String?,
    val acquirer: String?,
    val card: TransactionCard,
    val terminal: TransactionTerminal,
    val outlet: TransactionOutlet,
    val merchant: TransactionMerchant
)
