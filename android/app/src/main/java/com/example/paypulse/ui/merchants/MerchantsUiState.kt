package com.example.paypulse.ui.merchants

import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantStatus

data class MerchantsUiState(
    val searchText: String = "",
    val status: MerchantStatus? = null,
    val content: MerchantsContent = MerchantsContent.Loading
) {
    val hasFilters: Boolean
        get() = searchText.isNotBlank() || status != null
}

sealed interface MerchantsContent {
    data object Loading : MerchantsContent
    data object Empty : MerchantsContent
    data class Error(val error: MerchantError) : MerchantsContent
    data class Loaded(
        val merchants: List<Merchant>,
        val total: Long,
        val paging: MerchantsPaging
    ) : MerchantsContent
}

enum class MerchantsPaging { IDLE, LOADING_MORE, APPEND_ERROR, END }
