package com.example.paypulse.ui.outlets

import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletError

data class OutletsUiState(
    val merchantName: String = "",
    val searchText: String = "",
    val city: String? = null,
    val cities: List<String> = emptyList(),
    val content: OutletsContent = OutletsContent.Loading,
    val isRefreshing: Boolean = false,
    val refreshError: OutletError? = null
) {
    val hasFilters: Boolean
        get() = searchText.isNotBlank() || city != null
}

sealed interface OutletsContent {
    data object Loading : OutletsContent
    data object Empty : OutletsContent
    data class Error(val error: OutletError) : OutletsContent
    data class Loaded(
        val outlets: List<Outlet>,
        val total: Long,
        val paging: OutletsPaging
    ) : OutletsContent
}

enum class OutletsPaging { IDLE, LOADING_MORE, APPEND_ERROR, END }
