package com.example.paypulse.ui.transactions

import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.transaction.TransactionSort

sealed interface TransactionsEvent {
    data object MenuClicked : TransactionsEvent
    data class TransactionClicked(val id: Long) : TransactionsEvent
    data object Retry : TransactionsEvent
    data object LoadMore : TransactionsEvent
    data object ClearFilters : TransactionsEvent
    data class SearchChanged(val text: String) : TransactionsEvent
    data object SearchSubmitted : TransactionsEvent
    data object SearchCleared : TransactionsEvent
    data class ActiveFilterRemoved(val filter: ActiveFilter) : TransactionsEvent
    data object FilterButtonClicked : TransactionsEvent
}

sealed interface FilterSheetEvent : TransactionsEvent {
    data object Dismissed : FilterSheetEvent
    data class OptionToggled(val option: FilterOption) : FilterSheetEvent
    data class DatesChanged(val range: DateRange) : FilterSheetEvent
    data class AuthCodeChanged(val text: String) : FilterSheetEvent
    data class RrnChanged(val text: String) : FilterSheetEvent
    data class SortChanged(val sort: TransactionSort) : FilterSheetEvent
    data object ClearAll : FilterSheetEvent
    data object Apply : FilterSheetEvent
}
