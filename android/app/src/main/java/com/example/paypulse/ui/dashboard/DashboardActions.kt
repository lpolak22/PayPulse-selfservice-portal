package com.example.paypulse.ui.dashboard

import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.ui.dashboard.charts.DashboardChartCallbacks

class DashboardActions(
    val onMenuClick: () -> Unit,
    val onPeriodSelected: (DashboardPeriod) -> Unit,
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
    val onShowAllTransactions: () -> Unit,
    val charts: DashboardChartCallbacks
)
