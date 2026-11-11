package com.example.paypulse.ui.transactions

import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.domain.transaction.TransactionSort
import java.time.LocalDate

data class TransactionsUiState(
    val filters: TransactionFilters,
    val sort: TransactionSort = TransactionSort.DEFAULT,
    val searchText: String = "",
    val activeFilters: List<ActiveFilter> = emptyList(),
    val content: TransactionsContent = TransactionsContent.Loading,
    val filterDraft: FilterDraft? = null
) {
    val activeFilterCount: Int
        get() = activeFilters.size
}

sealed interface TransactionsContent {
    data object Loading : TransactionsContent
    data object Empty : TransactionsContent
    data class Error(val error: TransactionError) : TransactionsContent
    data class Loaded(
        val items: List<TransactionListItem>,
        val total: Long,
        val paging: PagingStatus
    ) : TransactionsContent
}

enum class PagingStatus { IDLE, LOADING_MORE, APPEND_ERROR, END }

data class FilterDraft(
    val filters: TransactionFilters,
    val sort: TransactionSort,
    val today: LocalDate
)
