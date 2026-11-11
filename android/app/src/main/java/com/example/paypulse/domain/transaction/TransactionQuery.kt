package com.example.paypulse.domain.transaction

data class TransactionQuery(
    val filters: TransactionFilters,
    val sort: TransactionSort,
    val page: Int,
    val pageSize: Int = DEFAULT_PAGE_SIZE
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
