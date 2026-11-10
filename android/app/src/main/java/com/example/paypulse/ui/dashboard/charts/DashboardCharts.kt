package com.example.paypulse.ui.dashboard.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardSummary
import com.example.paypulse.ui.dashboard.DashboardSelection

class DashboardChartCallbacks(
    val onTrendSelected: (Int) -> Unit,
    val onBrandSelected: (Int) -> Unit,
    val onAcquirerSelected: (Int) -> Unit
)

@Composable
fun DashboardCharts(
    summary: DashboardSummary,
    period: DashboardPeriod,
    selection: DashboardSelection,
    callbacks: DashboardChartCallbacks,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TrendSection(summary.trend, period, selection.trend, callbacks.onTrendSelected)
        BrandSection(summary.byCardBrand, selection.brand, callbacks.onBrandSelected)
        AcquirerSection(summary.byAcquirer, selection.acquirer, callbacks.onAcquirerSelected)
    }
}
