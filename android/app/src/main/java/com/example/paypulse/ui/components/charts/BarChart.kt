package com.example.paypulse.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val REGULAR_BAR_ALPHA = 0.7f

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    highlightedIndex: Int?,
    onBarClick: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val style = BarChartStyle(
        highlightedBar = colorScheme.primary,
        regularBar = colorScheme.primary.copy(alpha = REGULAR_BAR_ALPHA),
        label = colorScheme.onSurfaceVariant,
        highlightedLabel = colorScheme.onSurface,
        text = MaterialTheme.typography.labelSmall
    )
    val textMeasurer = rememberTextMeasurer()
    val currentOnBarClick by rememberUpdatedState(onBarClick)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .pointerInput(entries.size) {
                detectTapGestures { offset -> barIndexAt(offset.x, size.width.toFloat(), entries.size)?.let(currentOnBarClick) }
            },
        contentDescription = contentDescription
    ) {
        drawBarChart(entries, highlightedIndex, style, textMeasurer)
    }
}
