package com.example.paypulse.data.dashboard

import com.example.paypulse.domain.dashboard.AcquirerTotal
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.CardBrandTotal
import com.example.paypulse.domain.dashboard.DashboardSummary
import com.example.paypulse.domain.dashboard.DashboardTrend
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.dashboard.TrendPoint
import com.example.paypulse.domain.dashboard.TrendUnit
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

private class FakeProfile(
    val totalAmount: BigDecimal,
    val previousTotalAmount: BigDecimal,
    val totalCount: Int,
    val declinedCount: Int
)

private val todayProfile = FakeProfile(BigDecimal("6842.15"), BigDecimal("7105.90"), 182, 5)
private val weekProfile = FakeProfile(BigDecimal("48215.40"), BigDecimal("42894.48"), 1284, 28)
private val monthProfile = FakeProfile(BigDecimal("201730.85"), BigDecimal("189402.10"), 5391, 131)

private val hourlyWeights = listOf(0, 0, 0, 0, 0, 0, 1, 3, 6, 8, 9, 10, 12, 11, 9, 8, 9, 10, 9, 7, 4, 2, 1, 1)
private val weekdayWeights = listOf(61, 58, 69, 72, 90, 95, 37)
private val brandWeights = listOf(
    CardBrand.VISA to 46,
    CardBrand.MASTERCARD to 38,
    CardBrand.MAESTRO to 11,
    CardBrand.AMEX to 3,
    CardBrand.DINERS to 2
)
private val acquirerWeights = listOf("Acquirer A" to 52, "Acquirer B" to 31, "Acquirer C" to 17)

internal fun fakeSummaryFor(range: DateRange, now: Instant): DashboardSummary {
    val profile = profileFor(range)
    return DashboardSummary(
        totalAmount = profile.totalAmount,
        previousTotalAmount = profile.previousTotalAmount,
        totalCount = profile.totalCount,
        declinedCount = profile.declinedCount,
        trend = trendFor(range, profile),
        byCardBrand = brandTotals(profile),
        byAcquirer = acquirerTotals(profile),
        latestTransactions = fakeLatestTransactions(now)
    )
}

private fun profileFor(range: DateRange): FakeProfile = when {
    range.dayCount <= 1 -> todayProfile
    range.dayCount <= 7 -> weekProfile
    else -> monthProfile
}

private fun trendFor(range: DateRange, profile: FakeProfile): DashboardTrend {
    val start = range.from.atStartOfDay()
    if (range.dayCount <= 1) {
        val points = trendPoints(profile, hourlyWeights) { index -> start.plusHours(index.toLong()) }
        return DashboardTrend(TrendUnit.HOUR, points)
    }
    val weights = List(range.dayCount) { index -> weekdayWeights[(range.from.dayOfWeek.ordinal + index) % 7] }
    return DashboardTrend(TrendUnit.DAY, trendPoints(profile, weights) { index -> start.plusDays(index.toLong()) })
}

private fun trendPoints(
    profile: FakeProfile,
    weights: List<Int>,
    startOf: (Int) -> LocalDateTime
): List<TrendPoint> {
    val amounts = profile.totalAmount.splitBy(weights)
    val counts = profile.totalCount.splitBy(weights)
    return weights.indices.map { index -> TrendPoint(startOf(index), amounts[index], counts[index]) }
}

private fun brandTotals(profile: FakeProfile): List<CardBrandTotal> {
    val weights = brandWeights.map { it.second }
    val amounts = profile.totalAmount.splitBy(weights)
    val counts = profile.totalCount.splitBy(weights)
    return brandWeights.mapIndexed { index, (brand, _) -> CardBrandTotal(brand, amounts[index], counts[index]) }
}

private fun acquirerTotals(profile: FakeProfile): List<AcquirerTotal> {
    val weights = acquirerWeights.map { it.second }
    val amounts = profile.totalAmount.splitBy(weights)
    val counts = profile.totalCount.splitBy(weights)
    return acquirerWeights.mapIndexed { index, (name, _) -> AcquirerTotal(name, amounts[index], counts[index]) }
}
