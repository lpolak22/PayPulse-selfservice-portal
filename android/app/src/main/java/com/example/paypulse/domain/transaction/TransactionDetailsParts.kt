package com.example.paypulse.domain.transaction

import com.example.paypulse.domain.dashboard.CardBrand

data class TransactionCard(
    val brand: CardBrand,
    val maskedPan: String?
)

data class TransactionTerminal(
    val id: Long,
    val terminalId: String?,
    val type: String?
)

data class TransactionOutlet(
    val id: Long,
    val name: String?,
    val address: String?,
    val city: String?,
    val postalCode: String?
)

data class TransactionMerchant(
    val id: Long,
    val name: String?
)
