package com.example.paypulse.ui.transactions.details

import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.transactions.TransactionDateFormat

fun TransactionDetails.toDetailsModel(): TransactionDetailsModel {
    val localTime = transactionAt.atZone(DashboardPeriod.ZONE).toLocalDateTime()
    return TransactionDetailsModel(
        amount = CroatianFormat.money(amount, currency),
        status = status,
        date = TransactionDateFormat.date(localTime.toLocalDate()),
        time = TransactionDateFormat.timeWithSeconds(localTime),
        type = type,
        authCode = authCode,
        rrn = rrn,
        salesChannel = salesChannel,
        acquirer = acquirer,
        cardBrand = card.brand,
        cardNumber = cardNumberForDisplay(card.maskedPan),
        terminal = DetailsLink(terminal.id, listOfNotNull(terminal.terminalId, terminal.type)),
        outlet = DetailsLink(outlet.id, listOfNotNull(outlet.name, merchant.name))
    )
}
