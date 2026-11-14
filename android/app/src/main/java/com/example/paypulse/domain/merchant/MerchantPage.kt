package com.example.paypulse.domain.merchant

data class MerchantPage(
    val items: List<Merchant>,
    val page: Int,
    val pageSize: Int,
    val total: Long
) {
    val hasMore: Boolean
        get() = (page + 1).toLong() * pageSize < total
}
