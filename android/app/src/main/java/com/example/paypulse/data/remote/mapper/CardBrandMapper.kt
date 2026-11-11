package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.CardBrandTotalDto
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.CardBrandTotal
import java.math.BigDecimal

fun cardBrandOf(value: String?): CardBrand =
    CardBrand.entries.firstOrNull { it.name == value } ?: CardBrand.OTHER

fun List<CardBrandTotalDto>.toCardBrandTotals(): List<CardBrandTotal> =
    groupBy { cardBrandOf(it.brand) }.map { (brand, totals) ->
        CardBrandTotal(
            brand = brand,
            amount = totals.fold(BigDecimal.ZERO) { sum, total -> sum + total.amount },
            count = totals.sumOf { it.count }.toCount()
        )
    }
