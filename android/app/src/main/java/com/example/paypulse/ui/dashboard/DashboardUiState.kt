package com.example.paypulse.ui.dashboard

import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardSummary

data class DashboardUiState(
    val period: DashboardPeriod = DashboardPeriod.DEFAULT,
    val content: DashboardContentState = DashboardContentState.Loading,
    val isRefreshing: Boolean = false
)

sealed interface DashboardContentState {
    data object Loading : DashboardContentState
    data class Content(val summary: DashboardSummary, val period: DashboardPeriod) : DashboardContentState
    data object Empty : DashboardContentState
    data class Error(val error: DashboardError) : DashboardContentState
}

internal fun DashboardUiState.startLoading(): DashboardUiState = when (content) {
    is DashboardContentState.Content, DashboardContentState.Empty -> copy(isRefreshing = true)
    else -> copy(content = DashboardContentState.Loading, isRefreshing = false)
}
