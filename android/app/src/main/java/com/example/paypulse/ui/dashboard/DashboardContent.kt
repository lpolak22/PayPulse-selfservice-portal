package com.example.paypulse.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.ui.components.LoadingButton
import com.example.paypulse.ui.components.StatusMessage
import com.example.paypulse.ui.dashboard.charts.DashboardCharts
import com.example.paypulse.ui.dashboard.transactions.LatestTransactionsSection

@Composable
fun DashboardContent(state: DashboardUiState, actions: DashboardActions) {
    when (val content = state.content) {
        DashboardContentState.Loading -> DashboardSkeleton()
        is DashboardContentState.Content -> SummaryContent(content, state.selection, actions)
        DashboardContentState.Empty -> StatusMessage(
            icon = R.drawable.ic_inbox,
            title = stringResource(R.string.dashboard_empty_title),
            body = stringResource(R.string.dashboard_empty_hint)
        )
        is DashboardContentState.Error -> ErrorMessage(content.error, isRetrying = state.isRefreshing, onRetry = actions.onRetry)
    }
}

@Composable
private fun SummaryContent(content: DashboardContentState.Content, selection: DashboardSelection, actions: DashboardActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TurnoverCard(summary = content.summary, period = content.period)
        SummaryTiles(summary = content.summary)
        DashboardCharts(
            summary = content.summary,
            period = content.period,
            selection = selection,
            callbacks = actions.charts,
            modifier = Modifier.padding(top = 12.dp)
        )
        LatestTransactionsSection(
            transactions = content.summary.latestTransactions,
            today = content.today,
            onShowAll = actions.onShowAllTransactions,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ErrorMessage(error: DashboardError, isRetrying: Boolean, onRetry: () -> Unit) {
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body())
    ) {
        LoadingButton(
            text = stringResource(R.string.dashboard_retry),
            loadingDescription = stringResource(R.string.loading),
            isLoading = isRetrying,
            onClick = onRetry
        )
    }
}
