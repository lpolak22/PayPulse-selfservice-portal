package com.example.paypulse.domain.terminal

data class TerminalPage(
    val items: List<Terminal>,
    val page: Int,
    val pageSize: Int,
    val total: Long
) {
    val hasMore: Boolean
        get() = (page + 1).toLong() * pageSize < total
}
