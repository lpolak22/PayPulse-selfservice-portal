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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.ui.components.MessageBanner
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.components.SegmentedSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    firstName: String,
    actions: DashboardActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PayPulseTopBar(onMenuClick = actions.onMenuClick) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isPullRefreshing,
            onRefresh = actions.onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DashboardBody(state = state, firstName = firstName, actions = actions)
        }
    }
}

@Composable
private fun DashboardBody(state: DashboardUiState, firstName: String, actions: DashboardActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardHeader(firstName = firstName)
        SegmentedSwitch(
            options = DashboardPeriod.entries,
            selected = state.period,
            onSelect = actions.onPeriodSelected,
            label = { stringResource(it.label()) }
        )
        RefreshIndicator(visible = state.showsRefreshBar())
        state.bannerError?.let { ErrorBanner(error = it, onRetry = actions.onRetry) }
        DashboardContent(state = state, actions = actions)
    }
}

private fun DashboardUiState.showsRefreshBar(): Boolean =
    isRefreshing && !isPullRefreshing && content !is DashboardContentState.Error

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
private fun ErrorBanner(error: DashboardError, onRetry: () -> Unit) {
    MessageBanner(
        message = stringResource(error.bannerMessage()),
        actionLabel = stringResource(R.string.dashboard_retry),
        onAction = onRetry
    )
}
