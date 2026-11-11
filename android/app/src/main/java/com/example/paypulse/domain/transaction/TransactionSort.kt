package com.example.paypulse.domain.transaction

enum class TransactionSortField { DATE, AMOUNT }

enum class SortDirection { ASC, DESC }

enum class TransactionSort(val field: TransactionSortField, val direction: SortDirection) {
    NEWEST_FIRST(TransactionSortField.DATE, SortDirection.DESC),
    OLDEST_FIRST(TransactionSortField.DATE, SortDirection.ASC),
    HIGHEST_AMOUNT_FIRST(TransactionSortField.AMOUNT, SortDirection.DESC),
    LOWEST_AMOUNT_FIRST(TransactionSortField.AMOUNT, SortDirection.ASC);

    companion object {
        val DEFAULT = NEWEST_FIRST
    }
}
