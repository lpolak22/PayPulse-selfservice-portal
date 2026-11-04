package com.example.paypulse.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.ui.dashboard.charts.DashboardChartCallbacks

@Composable
fun DashboardRoute(
    firstName: String,
    onMenuClick: () -> Unit,
    onShowAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMenuClick by rememberUpdatedState(onMenuClick)
    val currentOnShowAll by rememberUpdatedState(onShowAllTransactions)
    val actions = remember(viewModel) {
        DashboardActions(
            onMenuClick = { currentOnMenuClick() },
            onPeriodSelected = viewModel::onPeriodSelected,
            onRetry = viewModel::onRetry,
            onRefresh = viewModel::onRefresh,
            onShowAllTransactions = { currentOnShowAll() },
            charts = DashboardChartCallbacks(viewModel::onTrendSelected, viewModel::onBrandSelected, viewModel::onAcquirerSelected)
        )
    }
    DashboardScreen(state = state, firstName = firstName, actions = actions, modifier = modifier)
}
