package com.example.paypulse.ui.transactions

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.Transaction
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.TransactionSortField
import com.example.paypulse.ui.components.CroatianFormat
import java.time.LocalDate
import java.time.LocalDateTime

private const val VISIBLE_PAN_DIGITS = 4

data class TransactionRowModel(
    val id: Long,
    val brand: CardBrand,
    val lastDigits: String?,
    val time: String,
    val dateTime: String,
    val terminalId: String?,
    val channel: SalesChannel?,
    val amount: String,
    val status: TransactionStatus?
)

sealed interface DayLabel {
    data object Today : DayLabel
    data object Yesterday : DayLabel
    data class Date(val text: String) : DayLabel
}

sealed interface TransactionListItem {
    val key: String

    data class DayHeader(val date: LocalDate, val label: DayLabel) : TransactionListItem {
        override val key: String = "day-$date"
    }

    data class Row(val row: TransactionRowModel) : TransactionListItem {
        override val key: String = "transaction-${row.id}"
    }
}

fun List<Transaction>.toListItems(sort: TransactionSort, today: LocalDate): List<TransactionListItem> =
    if (sort.field == TransactionSortField.DATE) groupedByDay(today)
    else map { TransactionListItem.Row(it.toRowModel(showDate = true)) }

private fun List<Transaction>.groupedByDay(today: LocalDate): List<TransactionListItem> =
    groupBy { it.localDateTime().toLocalDate() }.flatMap { (date, transactions) ->
        listOf(TransactionListItem.DayHeader(date, dayLabel(date, today))) +
            transactions.map { TransactionListItem.Row(it.toRowModel(showDate = false)) }
    }

private fun dayLabel(date: LocalDate, today: LocalDate): DayLabel = when (date) {
    today -> DayLabel.Today
    today.minusDays(1) -> DayLabel.Yesterday
    else -> DayLabel.Date(TransactionDateFormat.date(date))
}

private fun Transaction.toRowModel(showDate: Boolean): TransactionRowModel {
    val localTime = localDateTime()
    val dateTime = TransactionDateFormat.dateTime(localTime)
    return TransactionRowModel(
        id = id,
        brand = cardBrand,
        lastDigits = maskedPan?.takeIf { it.isNotBlank() }?.takeLast(VISIBLE_PAN_DIGITS),
        time = if (showDate) dateTime else TransactionDateFormat.time(localTime),
        dateTime = dateTime,
        terminalId = terminalId?.takeIf { it.isNotBlank() },
        channel = salesChannel,
        amount = CroatianFormat.money(amount),
        status = status
    )
}

private fun Transaction.localDateTime(): LocalDateTime = transactionAt.atZone(DashboardPeriod.ZONE).toLocalDateTime()
