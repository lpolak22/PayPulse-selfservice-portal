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
private val weekSummary = fakeSummaryFor(DashboardPeriod.LAST_7_DAYS.toDateRange(previewClock), previewClock.instant())
private val noChartCallbacks = DashboardChartCallbacks({}, {}, {})

@Preview(name = "Light", heightDp = 1500)
@Preview(name = "Dark", heightDp = 1500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardFilledPreview() = DashboardPreview(
    DashboardUiState(content = DashboardContentState.Content(weekSummary, DashboardPeriod.LAST_7_DAYS))
)

@Preview(name = "Light", heightDp = 1500)
@Preview(name = "Dark", heightDp = 1500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardSelectedPreview() = DashboardPreview(
    DashboardUiState(
        content = DashboardContentState.Content(weekSummary, DashboardPeriod.LAST_7_DAYS),
        selection = DashboardSelection(trend = 1, brand = 0, acquirer = 2)
    )
)

@PreviewLightDark
@Composable
private fun DashboardLoadingPreview() = DashboardPreview(DashboardUiState())

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
        DashboardScreen(
            state = state,
            firstName = "Ana",
            onMenuClick = {},
            onPeriodSelected = {},
            onRetry = {},
            chartCallbacks = noChartCallbacks
        )
    }
}
