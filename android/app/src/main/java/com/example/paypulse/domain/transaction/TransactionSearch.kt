package com.example.paypulse.domain.transaction

private val RRN_PATTERN = Regex("\\d{12}")

fun TransactionFilters.withSearch(text: String): TransactionFilters {
    val value = text.trim()
    return when {
        value.isEmpty() -> copy(authCode = null, rrn = null)
        RRN_PATTERN.matches(value) -> copy(authCode = null, rrn = value)
        else -> copy(authCode = value, rrn = null)
    }
}
