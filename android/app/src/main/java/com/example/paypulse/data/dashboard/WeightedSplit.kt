package com.example.paypulse.data.dashboard

import java.math.BigDecimal
import java.math.RoundingMode

internal fun BigDecimal.splitBy(weights: List<Int>): List<BigDecimal> {
    val weightSum = BigDecimal(weights.sum())
    val parts = weights.dropLast(1).map { multiply(BigDecimal(it)).divide(weightSum, 2, RoundingMode.DOWN) }
    return parts + subtract(parts.fold(BigDecimal.ZERO, BigDecimal::add))
}

internal fun Int.splitBy(weights: List<Int>): List<Int> {
    val weightSum = weights.sum()
    val parts = weights.dropLast(1).map { this * it / weightSum }
    return parts + (this - parts.sum())
}
