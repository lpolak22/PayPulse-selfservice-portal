package com.example.paypulse.domain.merchant

data class Merchant(
    val id: Long,
    val name: String,
    val oib: String,
    val status: MerchantStatus?,
    val outletCount: Long
)
