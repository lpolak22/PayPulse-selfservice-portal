package com.example.paypulse.ui.components.charts

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val BAR_WIDTH_RATIO = 0.7f

internal class BarChartStyle(
    val highlightedBar: Color,
    val regularBar: Color,
    val label: Color,
    val highlightedLabel: Color,
    val text: TextStyle
)

private class BarChartLayout(
    val slotWidth: Float,
    val barTop: Float,
    val barBottom: Float,
    val maxValue: Float,
    val labelGap: Float
) {
    fun barRect(index: Int, value: Float): Rect {
        val height = if (maxValue <= 0f) 0f else (barBottom - barTop) * (value / maxValue)
        val barWidth = slotWidth * BAR_WIDTH_RATIO
        val left = slotWidth * index + (slotWidth - barWidth) / 2
        return Rect(left, barBottom - height, left + barWidth, barBottom)
    }
}

internal fun barIndexAt(x: Float, width: Float, count: Int): Int? {
    if (count == 0 || width <= 0f) return null
    return (x / (width / count)).toInt().coerceIn(0, count - 1)
}

internal fun DrawScope.drawBarChart(
    entries: List<BarChartEntry>,
    highlightedIndex: Int?,
    style: BarChartStyle,
    textMeasurer: TextMeasurer
) {
    if (entries.isEmpty()) return
    val labelHeight = textMeasurer.measure("0", style.text).size.height.toFloat()
    val layout = barChartLayout(entries, labelHeight)
    entries.forEachIndexed { index, entry ->
        val isHighlighted = index == highlightedIndex
        val bar = layout.barRect(index, entry.value)
        drawBar(bar, if (isHighlighted) style.highlightedBar else style.regularBar)
        val textStyle = labelStyle(style, isHighlighted)
        entry.valueLabel?.let { drawCenteredText(textMeasurer, it, bar.center.x, bar.top - layout.labelGap - labelHeight, textStyle) }
        entry.axisLabel?.let { drawCenteredText(textMeasurer, it, bar.center.x, layout.barBottom + layout.labelGap, textStyle) }
    }
}

private fun DrawScope.barChartLayout(entries: List<BarChartEntry>, labelHeight: Float): BarChartLayout {
    val gap = 4.dp.toPx()
    val top = if (entries.any { it.valueLabel != null }) labelHeight + gap else 0f
    val bottomSpace = if (entries.any { it.axisLabel != null }) labelHeight + gap else 0f
    return BarChartLayout(
        slotWidth = size.width / entries.size,
        barTop = top,
        barBottom = size.height - bottomSpace,
        maxValue = entries.maxOf { it.value },
        labelGap = gap
    )
}

private fun labelStyle(style: BarChartStyle, isHighlighted: Boolean): TextStyle = style.text.copy(
    color = if (isHighlighted) style.highlightedLabel else style.label,
    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
)

private fun DrawScope.drawBar(rect: Rect, color: Color) {
    if (rect.height <= 0f) return
    val radius = minOf(rect.width / 2, 6.dp.toPx(), rect.height)
    val corner = CornerRadius(radius)
    val path = Path().apply { addRoundRect(RoundRect(rect, topLeft = corner, topRight = corner)) }
    drawPath(path, color)
}

private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    centerX: Float,
    top: Float,
    style: TextStyle
) {
    val layout = textMeasurer.measure(text, style)
    val maxLeft = (size.width - layout.size.width).coerceAtLeast(0f)
    val left = (centerX - layout.size.width / 2f).coerceIn(0f, maxLeft)
    drawText(layout, topLeft = Offset(left, top))
}
