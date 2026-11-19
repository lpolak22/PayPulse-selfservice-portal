package com.example.paypulse.ui.outlets

sealed interface OutletsEvent {
    data object BackClicked : OutletsEvent
    data class OutletClicked(val id: Long) : OutletsEvent
    data class SearchChanged(val text: String) : OutletsEvent
    data object SearchCleared : OutletsEvent
    data class CitySelected(val city: String?) : OutletsEvent
    data object ClearFilters : OutletsEvent
    data object Retry : OutletsEvent
    data object Refresh : OutletsEvent
    data object LoadMore : OutletsEvent
}
