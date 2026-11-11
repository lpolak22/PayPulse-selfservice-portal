package com.example.paypulse.ui.dashboard

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.data.dashboard.fakeSummaryFor
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.ui.dashboard.charts.DashboardChartCallbacks
import com.example.paypulse.ui.theme.PayPulseTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private val previewClock = Clock.fixed(Instant.parse("2026-11-03T13:00:00Z"), ZoneOffset.UTC)
private val weekRange = DashboardPeriod.LAST_7_DAYS.toDateRange(previewClock)
private val weekContent = DashboardContentState.Content(
    summary = fakeSummaryFor(weekRange, previewClock.instant()),
    period = DashboardPeriod.LAST_7_DAYS,
    today = weekRange.to
)
private val noActions = DashboardActions(
    onMenuClick = {},
    onPeriodSelected = {},
    onRetry = {},
    onRefresh = {},
    onShowAllTransactions = {},
    charts = DashboardChartCallbacks({}, {}, {})
)

@Preview(name = "Light", heightDp = 1900)
@Preview(name = "Dark", heightDp = 1900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardFilledPreview() = DashboardPreview(DashboardUiState(content = weekContent))

@Preview(name = "Light", heightDp = 1900)
@Preview(name = "Dark", heightDp = 1900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardSelectedPreview() = DashboardPreview(
    DashboardUiState(content = weekContent, selection = DashboardSelection(trend = 1, brand = 0, acquirer = 2))
)

@PreviewLightDark
@Composable
private fun DashboardErrorBannerPreview() = DashboardPreview(
    DashboardUiState(period = DashboardPeriod.TODAY, content = weekContent, bannerError = DashboardError.NETWORK)
)

@Preview(name = "Light", heightDp = 1400)
@Preview(name = "Dark", heightDp = 1400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardSkeletonPreview() = DashboardPreview(DashboardUiState())

@PreviewLightDark
@Composable
private fun DashboardEmptyPreview() = DashboardPreview(DashboardUiState(content = DashboardContentState.Empty))

@PreviewLightDark
@Composable
private fun DashboardNetworkErrorPreview() = DashboardPreview(
    DashboardUiState(content = DashboardContentState.Error(DashboardError.NETWORK))
)

@PreviewLightDark
@Composable
private fun DashboardUnknownErrorPreview() = DashboardPreview(
    DashboardUiState(content = DashboardContentState.Error(DashboardError.UNKNOWN))
)

@Composable
private fun DashboardPreview(state: DashboardUiState) {
    PayPulseTheme {
        DashboardScreen(state = state, firstName = "Ana", actions = noActions)
    }
}
