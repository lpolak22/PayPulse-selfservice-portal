package com.example.paypulse.ui.terminals

import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType

data class TerminalsUiState(
    val status: TerminalStatus? = null,
    val type: TerminalType? = null,
    val content: TerminalsContent = TerminalsContent.Loading,
    val isRefreshing: Boolean = false
) {
    val hasFilters: Boolean
        get() = status != null || type != null
}

sealed interface TerminalsContent {
    data object Loading : TerminalsContent
    data object Empty : TerminalsContent
    data class Error(val error: TerminalError) : TerminalsContent
    data class Loaded(
        val terminals: List<Terminal>,
        val total: Long,
        val paging: TerminalsPaging
    ) : TerminalsContent
}

enum class TerminalsPaging { IDLE, LOADING_MORE, APPEND_ERROR, END }
