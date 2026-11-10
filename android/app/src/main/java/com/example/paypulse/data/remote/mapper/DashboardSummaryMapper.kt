package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.AcquirerTotalDto
import com.example.paypulse.data.remote.dto.DashboardSummaryDto
import com.example.paypulse.data.remote.dto.LatestTransactionDto
import com.example.paypulse.data.remote.dto.TrendDto
import com.example.paypulse.data.remote.dto.TrendPointDto
import com.example.paypulse.domain.dashboard.AcquirerTotal
import com.example.paypulse.domain.dashboard.DashboardSummary
import com.example.paypulse.domain.dashboard.DashboardTrend
import com.example.paypulse.domain.dashboard.LatestTransaction
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.dashboard.TrendPoint
import com.example.paypulse.domain.dashboard.TrendUnit
import java.time.Instant
import java.time.LocalDateTime

fun DashboardSummaryDto.toDashboardSummary(): DashboardSummary = DashboardSummary(
    totalAmount = totalAmount,
    previousTotalAmount = previousTotalAmount,
    totalCount = totalCount.toCount(),
    declinedCount = declinedCount.toCount(),
    trend = trend.toDashboardTrend(),
    byCardBrand = byCardBrand.toCardBrandTotals(),
    byAcquirer = byAcquirer.map { it.toAcquirerTotal() },
    latestTransactions = latestTransactions.map { it.toLatestTransaction() }
)

internal fun Long.toCount(): Int = Math.toIntExact(this)

private fun TrendDto.toDashboardTrend(): DashboardTrend =
    DashboardTrend(unit = TrendUnit.valueOf(unit), points = points.map { it.toTrendPoint() })

private fun TrendPointDto.toTrendPoint(): TrendPoint =
    TrendPoint(start = LocalDateTime.parse(start), amount = amount, count = count.toCount())

private fun AcquirerTotalDto.toAcquirerTotal(): AcquirerTotal =
    AcquirerTotal(acquirer = acquirer, amount = amount, count = count.toCount())

private fun LatestTransactionDto.toLatestTransaction(): LatestTransaction = LatestTransaction(
    cardBrand = cardBrandOf(cardBrand),
    maskedPan = maskedPan,
    outletName = outletName,
    transactionAt = Instant.parse(transactionAt),
    amount = amount,
    status = TransactionStatus.valueOf(status)
)
