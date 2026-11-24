package com.example.paypulse.ui.transactions.details

private const val GROUP_SIZE = 4
private const val BACKEND_MASK = '*'
private const val DISPLAY_MASK = '•'

fun cardNumberForDisplay(maskedPan: String?): String? {
    if (maskedPan == null || BACKEND_MASK !in maskedPan) return null
    return maskedPan.replace(BACKEND_MASK, DISPLAY_MASK).chunked(GROUP_SIZE).joinToString(" ")
}
