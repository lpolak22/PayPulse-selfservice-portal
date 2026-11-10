package com.example.paypulse.domain.dashboard

import java.math.BigDecimal
import java.math.RoundingMode

private const val AMOUNT_SCALE = 2
private const val RATIO_SCALE = 6

data class DashboardSummary(
    val totalAmount: BigDecimal,
    val previousTotalAmount: BigDecimal,
    val totalCount: Int,
    val declinedCount: Int,
    val trend: DashboardTrend,
    val byCardBrand: List<CardBrandTotal>,
    val byAcquirer: List<AcquirerTotal>,
    val latestTransactions: List<LatestTransaction>
) {
    val hasNoTransactions: Boolean
        get() = totalCount == 0 && declinedCount == 0

    val averageAmount: BigDecimal?
        get() = if (totalCount == 0) null else totalAmount.divide(BigDecimal(totalCount), AMOUNT_SCALE, RoundingMode.HALF_UP)

    val successRate: BigDecimal?
        get() {
            val attempted = totalCount + declinedCount
            return if (attempted == 0) null else BigDecimal(totalCount).divide(BigDecimal(attempted), RATIO_SCALE, RoundingMode.HALF_UP)
        }

    val changeRatio: BigDecimal?
        get() = if (previousTotalAmount.signum() == 0) null
        else (totalAmount - previousTotalAmount).divide(previousTotalAmount, RATIO_SCALE, RoundingMode.HALF_UP)
}
