package com.example.paypulse.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.ui.dashboard.charts.DashboardChartCallbacks

@Composable
fun DashboardRoute(
    firstName: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val chartCallbacks = remember(viewModel) {
        DashboardChartCallbacks(viewModel::onTrendSelected, viewModel::onBrandSelected, viewModel::onAcquirerSelected)
    }
    DashboardScreen(
        state = state,
        firstName = firstName,
        onMenuClick = onMenuClick,
        onPeriodSelected = viewModel::onPeriodSelected,
        onRetry = viewModel::onRetry,
        chartCallbacks = chartCallbacks,
        modifier = modifier
    )
}
