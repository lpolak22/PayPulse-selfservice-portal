package com.example.paypulse.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun contrastRatio(foreground: Color, background: Color): Float {
    val first = relativeLuminance(foreground)
    val second = relativeLuminance(background)
    return ((max(first, second) + 0.05) / (min(first, second) + 0.05)).toFloat()
}

private fun relativeLuminance(color: Color): Double =
    0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)

private fun linear(channel: Float): Double =
    if (channel <= 0.04045f) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)
