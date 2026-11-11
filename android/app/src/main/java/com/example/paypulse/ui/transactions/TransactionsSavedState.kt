package com.example.paypulse.ui.transactions

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.TransactionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.DateTimeException
import java.time.LocalDate

data class RestoredTransactionsState(
    val filters: TransactionFilters,
    val sort: TransactionSort,
    val searchText: String
)

@Serializable
private data class SavedTransactionsState(
    val from: String,
    val to: String,
    val statuses: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val cardBrands: List<String> = emptyList(),
    val salesChannels: List<String> = emptyList(),
    val authCode: String? = null,
    val rrn: String? = null,
    val sort: String,
    val searchText: String = ""
)

fun encodeTransactionsState(filters: TransactionFilters, sort: TransactionSort, searchText: String): String =
    Json.encodeToString(
        SavedTransactionsState.serializer(),
        SavedTransactionsState(
            from = filters.dateRange.from.toString(),
            to = filters.dateRange.to.toString(),
            statuses = filters.statuses.map { it.name },
            types = filters.types.map { it.name },
            cardBrands = filters.cardBrands.map { it.name },
            salesChannels = filters.salesChannels.map { it.name },
            authCode = filters.authCode,
            rrn = filters.rrn,
            sort = sort.name,
            searchText = searchText
        )
    )

fun decodeTransactionsState(encoded: String): RestoredTransactionsState? = try {
    Json.decodeFromString(SavedTransactionsState.serializer(), encoded).toRestoredState()
} catch (exception: SerializationException) {
    null
} catch (exception: IllegalArgumentException) {
    null
} catch (exception: DateTimeException) {
    null
}

private fun SavedTransactionsState.toRestoredState(): RestoredTransactionsState = RestoredTransactionsState(
    filters = TransactionFilters(
        dateRange = DateRange(LocalDate.parse(from), LocalDate.parse(to)),
        statuses = statuses.toEnumSet<TransactionStatus>(),
        types = types.toEnumSet<TransactionType>(),
        cardBrands = cardBrands.toEnumSet<CardBrand>(),
        salesChannels = salesChannels.toEnumSet<SalesChannel>(),
        authCode = authCode,
        rrn = rrn
    ),
    sort = TransactionSort.entries.firstOrNull { it.name == sort } ?: TransactionSort.DEFAULT,
    searchText = searchText
)

private inline fun <reified T : Enum<T>> List<String>.toEnumSet(): Set<T> =
    mapNotNull { name -> enumValues<T>().firstOrNull { it.name == name } }.toSet()
