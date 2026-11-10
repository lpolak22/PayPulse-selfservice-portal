package com.example.paypulse.ui.components.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.hypot

private const val START_ANGLE = -90f
private const val GAP_DEGREES = 3f
private const val FULL_CIRCLE = 360f

internal class DonutStyle(val ringWidth: Dp, val selectedRingWidth: Dp, val borderColor: Color)

private class SliceArc(val startAngle: Float, val sweep: Float)

internal fun DrawScope.drawDonut(slices: List<DonutSlice>, selectedIndex: Int?, style: DonutStyle) {
    val arcs = sliceArcs(slices)
    val radius = (size.minDimension - style.selectedRingWidth.toPx()) / 2
    slices.forEachIndexed { index, slice ->
        val arc = arcs[index]
        val ringWidth = if (index == selectedIndex) style.selectedRingWidth else style.ringWidth
        if (slice.outlined) drawSliceBorder(arc, radius, ringWidth.toPx(), style.borderColor)
        drawSlice(arc, radius, ringWidth.toPx(), slice.color)
    }
}

private fun sliceArcs(slices: List<DonutSlice>): List<SliceArc> {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return slices.map { SliceArc(START_ANGLE, 0f) }
    val gap = if (slices.count { it.value > 0f } > 1) GAP_DEGREES else 0f
    var start = START_ANGLE
    return slices.map { slice ->
        val sweep = slice.value / total * FULL_CIRCLE
        SliceArc(start + gap / 2, (sweep - gap).coerceAtLeast(0f)).also { start += sweep }
    }
}

private fun DrawScope.drawSlice(arc: SliceArc, radius: Float, ringWidth: Float, color: Color) {
    if (arc.sweep <= 0f) return
    drawArc(
        color = color,
        startAngle = arc.startAngle,
        sweepAngle = arc.sweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = ringWidth)
    )
}

private fun DrawScope.drawSliceBorder(arc: SliceArc, radius: Float, ringWidth: Float, color: Color) {
    if (arc.sweep <= 0f) return
    val border = 1.dp.toPx()
    val borderDegrees = Math.toDegrees((border / radius).toDouble()).toFloat()
    val outlineArc = SliceArc(arc.startAngle - borderDegrees, arc.sweep + 2 * borderDegrees)
    drawSlice(outlineArc, radius, ringWidth + 2 * border, color)
}

internal fun donutSliceAt(offset: Offset, width: Float, slices: List<DonutSlice>, maxRingWidth: Float): Int? {
    val radius = (width - maxRingWidth) / 2
    val dx = offset.x - width / 2
    val dy = offset.y - width / 2
    if (hypot(dx, dy) !in (radius - maxRingWidth)..(radius + maxRingWidth)) return null
    val angle = (Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() - START_ANGLE + FULL_CIRCLE) % FULL_CIRCLE
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return null
    var end = 0f
    slices.forEachIndexed { index, slice ->
        end += slice.value / total * FULL_CIRCLE
        if (angle < end) return index
    }
    return slices.lastIndex
}
