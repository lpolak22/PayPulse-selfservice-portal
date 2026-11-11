package com.example.paypulse.domain.transaction

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.dashboard.TransactionStatus

data class TransactionFilters(
    val dateRange: DateRange,
    val statuses: Set<TransactionStatus> = emptySet(),
    val types: Set<TransactionType> = emptySet(),
    val cardBrands: Set<CardBrand> = emptySet(),
    val salesChannels: Set<SalesChannel> = emptySet(),
    val authCode: String? = null,
    val rrn: String? = null
) {
    fun normalized(): TransactionFilters = copy(authCode = authCode.cleaned(), rrn = rrn.cleaned())

    private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
