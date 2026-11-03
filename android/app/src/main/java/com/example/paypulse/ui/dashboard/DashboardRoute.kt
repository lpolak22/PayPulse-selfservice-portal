package com.example.paypulse.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardRoute(
    firstName: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        firstName = firstName,
        onMenuClick = onMenuClick,
        onPeriodSelected = viewModel::onPeriodSelected,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}
