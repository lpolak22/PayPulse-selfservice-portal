package com.example.paypulse.ui.dashboard.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardTrend
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.charts.BarChart

@Composable
fun TrendSection(trend: DashboardTrend, period: DashboardPeriod, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val chart = remember(trend, period) { trend.toTrendChart(period) }
    if (chart.entries.isEmpty()) return
    val title = stringResource(chart.title)
    ChartSection(title = title, detail = selectedIndex?.let(chart.details::getOrNull)) {
        BarChart(
            entries = chart.entries,
            highlightedIndex = selectedIndex ?: chart.peakIndex,
            onBarClick = onSelect,
            contentDescription = trendDescription(title, chart)
        )
    }
}

@Composable
private fun trendDescription(title: String, chart: TrendChart): String {
    val peak = chart.peakIndex?.let(chart.details::get) ?: return title
    val lowest = chart.lowestIndex?.let(chart.details::get) ?: return title
    return stringResource(
        R.string.dashboard_trend_description,
        title,
        peak.label,
        CroatianFormat.money(peak.amount),
        lowest.label,
        CroatianFormat.money(lowest.amount)
    )
}
