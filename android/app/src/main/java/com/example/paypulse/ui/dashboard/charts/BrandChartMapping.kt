package com.example.paypulse.ui.dashboard.charts

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.CardBrandTotal
import java.math.BigDecimal

private const val MAX_SEGMENTS = 4
private const val NAMED_SEGMENTS = MAX_SEGMENTS - 1

data class BrandChart(val details: List<ChartDetail>, val percents: List<Int>, val hasOther: Boolean)

fun List<CardBrandTotal>.toBrandChart(brandName: (CardBrand) -> String, otherLabel: String): BrandChart {
    val sorted = filter { it.amount.signum() > 0 }.sortedByDescending { it.amount }
    val (unnamed, known) = sorted.partition { it.brand == CardBrand.OTHER }
    val segmentCount = known.size + unnamed.size.coerceAtMost(1)
    val named = if (segmentCount > MAX_SEGMENTS) known.take(NAMED_SEGMENTS) else known
    val other = (known.drop(named.size) + unnamed).toOtherDetail(otherLabel)
    val details = named.map { ChartDetail(brandName(it.brand), it.amount, it.count) } + listOfNotNull(other)
    return BrandChart(
        details = details,
        percents = largestRemainderPercents(details.map { it.amount }),
        hasOther = other != null
    )
}

private fun List<CardBrandTotal>.toOtherDetail(otherLabel: String): ChartDetail? {
    if (isEmpty()) return null
    return ChartDetail(
        label = otherLabel,
        amount = fold(BigDecimal.ZERO) { sum, total -> sum + total.amount },
        count = sumOf { it.count }
    )
}
