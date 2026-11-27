package com.example.paypulse.domain.terminal

data class TerminalQuery(
    val status: TerminalStatus? = null,
    val type: TerminalType? = null,
    val page: Int = 0,
    val pageSize: Int = DEFAULT_PAGE_SIZE
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
