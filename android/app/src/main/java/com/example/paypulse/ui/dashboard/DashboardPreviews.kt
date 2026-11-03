package com.example.paypulse.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardSummary
import com.example.paypulse.domain.dashboard.DashboardTrend
import com.example.paypulse.domain.dashboard.TrendUnit
import com.example.paypulse.ui.theme.PayPulseTheme
import java.math.BigDecimal

private val weekSummary = DashboardSummary(
    totalAmount = BigDecimal("48215.40"),
    previousTotalAmount = BigDecimal("42894.48"),
    totalCount = 1284,
    declinedCount = 28,
    trend = DashboardTrend(TrendUnit.DAY, emptyList()),
    byCardBrand = emptyList(),
    byAcquirer = emptyList(),
    latestTransactions = emptyList()
)

@PreviewLightDark
@Composable
private fun DashboardLoadingPreview() = DashboardPreview(DashboardUiState())

@PreviewLightDark
@Composable
private fun DashboardFilledPreview() = DashboardPreview(
    DashboardUiState(content = DashboardContentState.Content(weekSummary, DashboardPeriod.LAST_7_DAYS))
)

@PreviewLightDark
@Composable
private fun DashboardEmptyPreview() = DashboardPreview(DashboardUiState(content = DashboardContentState.Empty))

@PreviewLightDark
@Composable
private fun DashboardErrorPreview() = DashboardPreview(
    DashboardUiState(content = DashboardContentState.Error(DashboardError.NETWORK))
)

@Composable
private fun DashboardPreview(state: DashboardUiState) {
    PayPulseTheme {
        DashboardScreen(state = state, firstName = "Ana", onMenuClick = {}, onPeriodSelected = {}, onRetry = {})
    }
}
