package com.example.paypulse.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.paypulse.ui.theme.PayPulseTheme

private val weekBars = listOf(6.1f, 5.8f, 6.9f, 7.2f, 9.0f, 9.5f, 3.7f)
    .zip(listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned"))
    .map { (value, day) -> BarChartEntry(value, axisLabel = day, valueLabel = "${value.toString().replace('.', ',')}k") }

private val acquirers = listOf(
    HorizontalBarItem("Acquirer A", "25.072,01 €", 25072f),
    HorizontalBarItem("Acquirer B", "14.946,77 €", 14947f),
    HorizontalBarItem("Acquirer C", "8.196,62 €", 8197f)
)

@PreviewLightDark
@Composable
private fun BarChartPreview() = ChartPreview { BarChart(weekBars, highlightedIndex = 5, onBarClick = {}, contentDescription = "") }

@PreviewLightDark
@Composable
private fun BarChartSelectedPreview() = ChartPreview { BarChart(weekBars, highlightedIndex = 1, onBarClick = {}, contentDescription = "") }

@PreviewLightDark
@Composable
private fun BarChartSinglePreview() = ChartPreview {
    BarChart(listOf(BarChartEntry(4.2f, "Pon", "4,2k")), highlightedIndex = 0, onBarClick = {}, contentDescription = "")
}

@PreviewLightDark
@Composable
private fun DonutPreview() = ChartPreview { DonutWithLegend(donutSlices(), selectedIndex = null) }

@PreviewLightDark
@Composable
private fun DonutSelectedPreview() = ChartPreview { DonutWithLegend(donutSlices(), selectedIndex = 1) }

@PreviewLightDark
@Composable
private fun DonutSinglePreview() = ChartPreview { DonutWithLegend(donutSlices().take(1), selectedIndex = null) }

@PreviewLightDark
@Composable
private fun BarListPreview() = ChartPreview { HorizontalBarList(acquirers, selectedIndex = null, onItemClick = {}, contentDescription = "") }

@PreviewLightDark
@Composable
private fun BarListSelectedPreview() = ChartPreview { HorizontalBarList(acquirers, selectedIndex = 1, onItemClick = {}, contentDescription = "") }

@PreviewLightDark
@Composable
private fun BarListSinglePreview() = ChartPreview { HorizontalBarList(acquirers.take(1), selectedIndex = null, onItemClick = {}, contentDescription = "") }

@Composable
private fun donutSlices(): List<DonutSlice> {
    val colors = MaterialTheme.colorScheme
    return listOf(
        DonutSlice(46f, colors.primary),
        DonutSlice(38f, colors.onSurface),
        DonutSlice(11f, colors.outline),
        DonutSlice(5f, colors.outlineVariant, outlined = true)
    )
}

@Composable
private fun DonutWithLegend(slices: List<DonutSlice>, selectedIndex: Int?) {
    val labels = listOf("Visa", "Mastercard", "Maestro", "Ostalo")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DonutChart(slices, "brendova", "${slices.size}", selectedIndex, onSliceClick = {}, contentDescription = "")
        ChartLegend(
            items = slices.mapIndexed { i, slice -> LegendItem(labels[i], "${slice.value.toInt()} %", slice.color, slice.outlined) },
            selectedIndex = selectedIndex,
            onItemClick = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChartPreview(content: @Composable () -> Unit) {
    PayPulseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}
