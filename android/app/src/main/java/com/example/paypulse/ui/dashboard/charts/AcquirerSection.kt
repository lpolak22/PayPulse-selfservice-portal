package com.example.paypulse.ui.dashboard.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.AcquirerTotal
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.charts.HorizontalBarList

@Composable
fun AcquirerSection(totals: List<AcquirerTotal>, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val chart = remember(totals) { totals.toAcquirerChart() }
    val top = chart.details.firstOrNull() ?: return
    val title = stringResource(R.string.dashboard_acquirer_title)
    ChartSection(title = title, detail = selectedIndex?.let(chart.details::getOrNull)) {
        HorizontalBarList(
            items = chart.items,
            selectedIndex = selectedIndex,
            onItemClick = onSelect,
            contentDescription = stringResource(
                R.string.dashboard_acquirer_description,
                title,
                top.label,
                CroatianFormat.money(top.amount)
            )
        )
    }
}
