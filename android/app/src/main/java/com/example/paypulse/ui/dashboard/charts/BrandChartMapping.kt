package com.example.paypulse.ui.dashboard.charts

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.CardBrandTotal
import java.math.BigDecimal

private const val MAX_SEGMENTS = 4
private const val NAMED_SEGMENTS = MAX_SEGMENTS - 1

data class BrandChart(val details: List<ChartDetail>, val percents: List<Int>)

fun List<CardBrandTotal>.toBrandChart(brandName: (CardBrand) -> String, otherLabel: String): BrandChart {
    val sorted = filter { it.amount.signum() > 0 }.sortedByDescending { it.amount }
    val named = if (sorted.size > MAX_SEGMENTS) sorted.take(NAMED_SEGMENTS) else sorted
    val details = named.map { ChartDetail(brandName(it.brand), it.amount, it.count) } +
        listOfNotNull(sorted.drop(named.size).toOtherDetail(otherLabel))
    return BrandChart(details = details, percents = largestRemainderPercents(details.map { it.amount }))
}

private fun List<CardBrandTotal>.toOtherDetail(otherLabel: String): ChartDetail? {
    if (isEmpty()) return null
    return ChartDetail(
        label = otherLabel,
        amount = fold(BigDecimal.ZERO) { sum, total -> sum + total.amount },
        count = sumOf { it.count }
    )
}
