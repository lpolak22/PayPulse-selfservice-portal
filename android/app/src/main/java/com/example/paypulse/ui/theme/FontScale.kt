package com.example.paypulse.ui.theme

private const val LARGER_TEXT_MULTIPLIER = 1.3f
private const val MAX_LARGER_TEXT_SCALE = 2.0f

fun effectiveFontScale(systemFontScale: Float, largerText: Boolean): Float =
    if (!largerText) systemFontScale
    else maxOf(systemFontScale, minOf(systemFontScale * LARGER_TEXT_MULTIPLIER, MAX_LARGER_TEXT_SCALE))
