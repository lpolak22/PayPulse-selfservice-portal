package com.example.paypulse.ui.merchants.details

sealed interface MerchantDetailsEvent {
    data object BackClicked : MerchantDetailsEvent
    data object MerchantsClicked : MerchantDetailsEvent
    data class TabSelected(val tab: MerchantDetailsTab) : MerchantDetailsEvent
    data class SearchChanged(val text: String) : MerchantDetailsEvent
    data object SearchCleared : MerchantDetailsEvent
    data class OutletClicked(val id: Long) : MerchantDetailsEvent
    data class TerminalClicked(val id: Long) : MerchantDetailsEvent
    data object Retry : MerchantDetailsEvent
}
