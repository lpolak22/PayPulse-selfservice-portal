package com.example.paypulse.ui.dashboard

import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardSummary
import java.time.LocalDate

data class DashboardUiState(
    val period: DashboardPeriod = DashboardPeriod.DEFAULT,
    val content: DashboardContentState = DashboardContentState.Loading,
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val bannerError: DashboardError? = null,
    val selection: DashboardSelection = DashboardSelection()
)

data class DashboardSelection(
    val trend: Int? = null,
    val brand: Int? = null,
    val acquirer: Int? = null
)

sealed interface DashboardContentState {
    data object Loading : DashboardContentState
    data class Content(val summary: DashboardSummary, val period: DashboardPeriod, val today: LocalDate) : DashboardContentState
    data object Empty : DashboardContentState
    data class Error(val error: DashboardError) : DashboardContentState
}

internal fun DashboardUiState.startLoading(fromPull: Boolean): DashboardUiState =
    if (content == DashboardContentState.Loading) copy(bannerError = null)
    else copy(isRefreshing = true, isPullRefreshing = fromPull, bannerError = null)

internal fun DashboardUiState.finishLoading(result: DashboardContentState): DashboardUiState {
    val keepsOldContent = content is DashboardContentState.Content || content == DashboardContentState.Empty
    val finished = copy(isRefreshing = false, isPullRefreshing = false)
    return if (result is DashboardContentState.Error && keepsOldContent) finished.copy(bannerError = result.error)
    else finished.copy(content = result, bannerError = null)
}
