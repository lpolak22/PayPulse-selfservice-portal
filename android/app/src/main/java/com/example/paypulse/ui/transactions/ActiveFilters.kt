package com.example.paypulse.ui.transactions

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.domain.transaction.TransactionType

sealed interface ActiveFilter {
    data class Dates(val range: DateRange) : ActiveFilter
    data class AuthCode(val value: String) : ActiveFilter
    data class Rrn(val value: String) : ActiveFilter
}

sealed interface FilterOption : ActiveFilter {
    data class Status(val value: TransactionStatus) : FilterOption
    data class Type(val value: TransactionType) : FilterOption
    data class Brand(val value: CardBrand) : FilterOption
    data class Channel(val value: SalesChannel) : FilterOption
}

val statusOptions: List<FilterOption> = TransactionStatus.entries.map { FilterOption.Status(it) }
val typeOptions: List<FilterOption> = (TransactionType.entries - TransactionType.OTHER).map { FilterOption.Type(it) }
val brandOptions: List<FilterOption> = (CardBrand.entries - CardBrand.OTHER).map { FilterOption.Brand(it) }
val channelOptions: List<FilterOption> = SalesChannel.entries.map { FilterOption.Channel(it) }

fun TransactionFilters.activeFilters(defaultRange: DateRange): List<ActiveFilter> = buildList {
    if (dateRange != defaultRange) add(ActiveFilter.Dates(dateRange))
    statuses.forEach { add(FilterOption.Status(it)) }
    types.forEach { add(FilterOption.Type(it)) }
    cardBrands.forEach { add(FilterOption.Brand(it)) }
    salesChannels.forEach { add(FilterOption.Channel(it)) }
    authCode?.let { add(ActiveFilter.AuthCode(it)) }
    rrn?.let { add(ActiveFilter.Rrn(it)) }
}

fun TransactionFilters.isSelected(option: FilterOption): Boolean = when (option) {
    is FilterOption.Status -> option.value in statuses
    is FilterOption.Type -> option.value in types
    is FilterOption.Brand -> option.value in cardBrands
    is FilterOption.Channel -> option.value in salesChannels
}

fun TransactionFilters.toggle(option: FilterOption): TransactionFilters = when (option) {
    is FilterOption.Status -> copy(statuses = statuses.toggle(option.value))
    is FilterOption.Type -> copy(types = types.toggle(option.value))
    is FilterOption.Brand -> copy(cardBrands = cardBrands.toggle(option.value))
    is FilterOption.Channel -> copy(salesChannels = salesChannels.toggle(option.value))
}

fun TransactionFilters.without(filter: ActiveFilter, defaultRange: DateRange): TransactionFilters = when (filter) {
    is ActiveFilter.Dates -> copy(dateRange = defaultRange)
    is ActiveFilter.AuthCode -> copy(authCode = null)
    is ActiveFilter.Rrn -> copy(rrn = null)
    is FilterOption -> if (isSelected(filter)) toggle(filter) else this
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
