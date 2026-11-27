package com.example.paypulse.ui.transactions.details

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionType

data class TransactionDetailsModel(
    val amount: String,
    val status: TransactionStatus?,
    val date: String,
    val time: String,
    val type: TransactionType,
    val authCode: String?,
    val rrn: String?,
    val salesChannel: SalesChannel?,
    val acquirer: String?,
    val cardBrand: CardBrand,
    val cardNumber: String?,
    val terminal: DetailsLink,
    val outlet: DetailsLink
)

data class DetailsLink(
    val id: Long,
    val labelParts: List<String>
)
