package com.example.paypulse.domain.merchant

data class MerchantQuery(
    val search: String = "",
    val status: MerchantStatus? = null,
    val page: Int = 0,
    val pageSize: Int = DEFAULT_PAGE_SIZE
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
