package com.example.paypulse.ui.terminals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType

val statusFilters: List<TerminalStatus?> = listOf(null) + TerminalStatus.entries
val typeFilters: List<TerminalType?> = listOf(null) + TerminalType.entries

@StringRes
fun TerminalStatus?.filterLabel(): Int = when (this) {
    null -> R.string.terminals_filter_all_statuses
    TerminalStatus.PREPARED -> R.string.terminals_filter_prepared
    TerminalStatus.AUTHENTICATED -> R.string.terminals_filter_authenticated
    TerminalStatus.ACTIVE -> R.string.terminals_filter_active
    TerminalStatus.INACTIVE -> R.string.terminals_filter_inactive
    TerminalStatus.DEACTIVATED -> R.string.terminals_filter_deactivated
    TerminalStatus.SUSPENDED -> R.string.terminals_filter_suspended
    TerminalStatus.BLOCKED -> R.string.terminals_filter_blocked
    TerminalStatus.UNKNOWN -> R.string.terminals_filter_unknown
}

@StringRes
fun TerminalStatus.badgeLabel(): Int = when (this) {
    TerminalStatus.PREPARED -> R.string.terminals_status_prepared
    TerminalStatus.AUTHENTICATED -> R.string.terminals_status_authenticated
    TerminalStatus.ACTIVE -> R.string.terminals_status_active
    TerminalStatus.INACTIVE -> R.string.terminals_status_inactive
    TerminalStatus.DEACTIVATED -> R.string.terminals_status_deactivated
    TerminalStatus.SUSPENDED -> R.string.terminals_status_suspended
    TerminalStatus.BLOCKED -> R.string.terminals_status_blocked
    TerminalStatus.UNKNOWN -> R.string.terminals_status_unknown
}

@DrawableRes
fun TerminalError.icon(): Int = when (this) {
    TerminalError.NETWORK -> R.drawable.ic_cloud_off
    TerminalError.INVALID_FILTER, TerminalError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun TerminalError.title(): Int = when (this) {
    TerminalError.NETWORK -> R.string.terminals_error_network_title
    TerminalError.INVALID_FILTER -> R.string.terminals_error_invalid_filter_title
    TerminalError.UNKNOWN -> R.string.terminals_error_unknown_title
}

@StringRes
fun TerminalError.body(): Int = when (this) {
    TerminalError.NETWORK -> R.string.terminals_error_network_body
    TerminalError.INVALID_FILTER -> R.string.terminals_error_invalid_filter_body
    TerminalError.UNKNOWN -> R.string.terminals_error_unknown_body
}
