package com.example.paypulse.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.components.SegmentedSwitch
import com.example.paypulse.ui.dashboard.charts.DashboardChartCallbacks
import com.example.paypulse.ui.dashboard.charts.DashboardCharts

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    firstName: String,
    onMenuClick: () -> Unit,
    onPeriodSelected: (DashboardPeriod) -> Unit,
    onRetry: () -> Unit,
    chartCallbacks: DashboardChartCallbacks,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PayPulseTopBar(onMenuClick = onMenuClick) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardHeader(firstName = firstName)
            SegmentedSwitch(
                options = DashboardPeriod.entries,
                selected = state.period,
                onSelect = onPeriodSelected,
                label = { stringResource(it.label()) }
            )
            RefreshIndicator(visible = state.isRefreshing)
            DashboardContent(state.content, state.selection, chartCallbacks, onRetry)
        }
    }
}

@Composable
private fun DashboardHeader(firstName: String) {
    Column {
        Text(
            text = stringResource(R.string.dashboard_greeting, firstName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun RefreshIndicator(visible: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().height(4.dp)) {
        if (visible) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun DashboardContent(
    content: DashboardContentState,
    selection: DashboardSelection,
    chartCallbacks: DashboardChartCallbacks,
    onRetry: () -> Unit
) {
    when (content) {
        DashboardContentState.Loading -> DashboardLoading()
        is DashboardContentState.Content -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TurnoverCard(summary = content.summary, period = content.period)
            SummaryTiles(summary = content.summary)
            DashboardCharts(
                summary = content.summary,
                period = content.period,
                selection = selection,
                callbacks = chartCallbacks,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        DashboardContentState.Empty -> DashboardMessage(text = stringResource(R.string.dashboard_empty))
        is DashboardContentState.Error -> DashboardMessage(
            text = stringResource(content.error.message()),
            onRetry = onRetry
        )
    }
}
