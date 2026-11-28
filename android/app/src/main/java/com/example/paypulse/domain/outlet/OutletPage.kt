package com.example.paypulse.domain.outlet

data class OutletPage(
    val items: List<Outlet>,
    val page: Int,
    val pageSize: Int,
    val total: Long
) {
    val hasMore: Boolean
        get() = (page + 1).toLong() * pageSize < total
}
