package com.example.paypulse.ui.dashboard.charts

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.CardBrandTotal
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.charts.ChartLegend
import com.example.paypulse.ui.components.charts.DonutChart
import com.example.paypulse.ui.components.charts.DonutSlice
import com.example.paypulse.ui.components.charts.LegendItem

private class SegmentColor(val color: Color, val outlined: Boolean = false)

@Composable
fun BrandSection(totals: List<CardBrandTotal>, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val names = CardBrand.entries.associateWith { stringResource(it.label()) }
    val otherLabel = stringResource(R.string.dashboard_brand_other)
    val chart = remember(totals, names, otherLabel) { totals.toBrandChart(names::getValue, otherLabel) }
    if (chart.details.isEmpty()) return
    val palette = segmentPalette()
    val title = stringResource(R.string.dashboard_brand_title)
    ChartSection(title = title, detail = selectedIndex?.let(chart.details::getOrNull)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BrandDonut(chart, palette, title, selectedIndex, onSelect)
            ChartLegend(
                items = legendItems(chart, palette),
                selectedIndex = selectedIndex,
                onItemClick = onSelect,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BrandDonut(
    chart: BrandChart,
    palette: List<SegmentColor>,
    title: String,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    DonutChart(
        slices = chart.details.mapIndexed { i, detail -> DonutSlice(detail.amount.toFloat(), palette[i].color, palette[i].outlined) },
        centerLabel = stringResource(R.string.dashboard_brand_center_label),
        centerValue = CroatianFormat.count(chart.details.size),
        selectedIndex = selectedIndex,
        onSliceClick = onSelect,
        contentDescription = brandDescription(title, chart)
    )
}

@Composable
private fun segmentPalette(): List<SegmentColor> {
    val colors = MaterialTheme.colorScheme
    return listOf(
        SegmentColor(colors.primary),
        SegmentColor(colors.onSurface),
        SegmentColor(colors.outline),
        SegmentColor(colors.outlineVariant, outlined = true)
    )
}

private fun legendItems(chart: BrandChart, palette: List<SegmentColor>): List<LegendItem> =
    chart.details.mapIndexed { i, detail ->
        LegendItem(detail.label, CroatianFormat.wholePercent(chart.percents[i]), palette[i].color, palette[i].outlined)
    }

@Composable
private fun brandDescription(title: String, chart: BrandChart): String {
    val shares = chart.details.mapIndexed { i, detail ->
        stringResource(R.string.dashboard_chart_share, detail.label, CroatianFormat.wholePercent(chart.percents[i]))
    }
    return stringResource(R.string.dashboard_brand_description, title, shares.joinToString(", "))
}

@StringRes
private fun CardBrand.label(): Int = when (this) {
    CardBrand.VISA -> R.string.dashboard_brand_visa
    CardBrand.MASTERCARD -> R.string.dashboard_brand_mastercard
    CardBrand.AMEX -> R.string.dashboard_brand_amex
    CardBrand.DINERS -> R.string.dashboard_brand_diners
    CardBrand.MAESTRO -> R.string.dashboard_brand_maestro
}
