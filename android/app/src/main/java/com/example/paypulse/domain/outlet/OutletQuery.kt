package com.example.paypulse.domain.outlet

data class OutletQuery(
    val merchantId: Long,
    val search: String = "",
    val city: String? = null,
    val page: Int = 0,
    val pageSize: Int = DEFAULT_PAGE_SIZE
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
