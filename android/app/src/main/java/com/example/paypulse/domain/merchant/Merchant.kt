package com.example.paypulse.domain.merchant

data class Merchant(
    val id: Long,
    val merchantId: String?,
    val name: String,
    val oib: String,
    val status: MerchantStatus?,
    val outletCount: Long,
    val terminalCount: Long
)
