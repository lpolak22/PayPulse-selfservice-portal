package com.example.paypulse.data.remote.dto

import com.example.paypulse.data.remote.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class DashboardSummaryDto(
    @Serializable(with = BigDecimalSerializer::class) val totalAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val previousTotalAmount: BigDecimal,
    val totalCount: Long,
    val declinedCount: Long,
    val trend: TrendDto,
    val byCardBrand: List<CardBrandTotalDto>,
    val byAcquirer: List<AcquirerTotalDto>,
    val latestTransactions: List<LatestTransactionDto>
)
