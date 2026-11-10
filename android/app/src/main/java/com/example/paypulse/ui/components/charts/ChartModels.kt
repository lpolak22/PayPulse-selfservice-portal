package com.example.paypulse.ui.components.charts

import androidx.compose.ui.graphics.Color

data class BarChartEntry(val value: Float, val axisLabel: String? = null, val valueLabel: String? = null)

data class DonutSlice(val value: Float, val color: Color, val outlined: Boolean = false)

data class LegendItem(val label: String, val value: String, val color: Color, val outlined: Boolean = false)

data class HorizontalBarItem(val label: String, val valueLabel: String, val value: Float)
