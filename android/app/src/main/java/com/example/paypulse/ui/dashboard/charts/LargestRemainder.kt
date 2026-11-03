package com.example.paypulse.ui.dashboard.charts

import java.math.BigDecimal
import java.math.RoundingMode

private const val PERCENT_TOTAL = 100
private const val EXACT_SCALE = 6

fun largestRemainderPercents(amounts: List<BigDecimal>): List<Int> {
    val total = amounts.fold(BigDecimal.ZERO, BigDecimal::add)
    if (total.signum() <= 0) return amounts.map { 0 }
    val exact = amounts.map { it.multiply(BigDecimal(PERCENT_TOTAL)).divide(total, EXACT_SCALE, RoundingMode.HALF_UP) }
    val floors = exact.map { it.setScale(0, RoundingMode.DOWN).toInt() }
    val missing = PERCENT_TOTAL - floors.sum()
    val bonus = exact.indices
        .sortedByDescending { exact[it] - BigDecimal(floors[it]) }
        .take(missing)
        .toSet()
    return floors.mapIndexed { index, floor -> if (index in bonus) floor + 1 else floor }
}
