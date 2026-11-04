package com.example.paypulse.ui.dashboard.transactions

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.LatestTransaction
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.ui.components.CroatianDateFormat
import com.example.paypulse.ui.components.CroatianFormat
import java.time.LocalDate

private const val VISIBLE_PAN_DIGITS = 4

data class LatestTransactionRowModel(
    val brand: CardBrand,
    val lastDigits: String,
    val outletName: String,
    val time: String,
    val amount: String,
    val status: TransactionStatus
)

fun LatestTransaction.toRowModel(today: LocalDate): LatestTransactionRowModel {
    val localTime = transactionAt.atZone(DashboardPeriod.ZONE).toLocalDateTime()
    return LatestTransactionRowModel(
        brand = cardBrand,
        lastDigits = maskedPan.takeLast(VISIBLE_PAN_DIGITS),
        outletName = outletName,
        time = if (localTime.toLocalDate() == today) CroatianDateFormat.time(localTime) else CroatianDateFormat.dateTime(localTime),
        amount = CroatianFormat.money(amount),
        status = status
    )
}
