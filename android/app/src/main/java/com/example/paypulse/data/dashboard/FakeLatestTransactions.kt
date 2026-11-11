package com.example.paypulse.data.dashboard

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.LatestTransaction
import com.example.paypulse.domain.dashboard.TransactionStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

internal fun fakeLatestTransactions(now: Instant): List<LatestTransaction> = listOf(
    fakeTransaction(CardBrand.VISA, "411111******4821", "PM Ilica 12", now, 12, "42.90", TransactionStatus.APPROVED),
    fakeTransaction(CardBrand.MASTERCARD, "522222******1907", "PM Avenue Mall", now, 26, "118.00", TransactionStatus.APPROVED),
    fakeTransaction(CardBrand.MAESTRO, "633333******3360", "PM Ilica 12", now, 39, "9.50", TransactionStatus.DECLINED),
    fakeTransaction(CardBrand.AMEX, "343434*****1005", "PM Savska 41", now, 58, "76.20", TransactionStatus.APPROVED),
    fakeTransaction(CardBrand.OTHER, null, "PM Avenue Mall", now, 71, "23.40", TransactionStatus.APPROVED)
)

private fun fakeTransaction(
    brand: CardBrand,
    maskedPan: String?,
    outletName: String,
    now: Instant,
    minutesAgo: Long,
    amount: String,
    status: TransactionStatus
) = LatestTransaction(
    cardBrand = brand,
    maskedPan = maskedPan,
    outletName = outletName,
    transactionAt = now.minus(minutesAgo, ChronoUnit.MINUTES),
    amount = BigDecimal(amount),
    status = status
)
