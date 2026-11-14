package com.example.paypulse.ui.merchants

import com.example.paypulse.domain.merchant.MerchantStatus

sealed interface MerchantsEvent {
    data object MenuClicked : MerchantsEvent
    data class MerchantClicked(val id: Long) : MerchantsEvent
    data class SearchChanged(val text: String) : MerchantsEvent
    data object SearchCleared : MerchantsEvent
    data class StatusSelected(val status: MerchantStatus?) : MerchantsEvent
    data object ClearFilters : MerchantsEvent
    data object Retry : MerchantsEvent
    data object LoadMore : MerchantsEvent
}
