package com.example.paypulse.ui.transactions.details

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.ui.dashboard.color
import com.example.paypulse.ui.dashboard.label

@StringRes
fun TransactionStatus?.badgeLabel(): Int = this?.label() ?: R.string.transactions_status_unknown

@DrawableRes
fun TransactionStatus?.badgeIcon(): Int = when (this) {
    TransactionStatus.APPROVED -> R.drawable.ic_check
    TransactionStatus.DECLINED -> R.drawable.ic_close
    TransactionStatus.REFUNDED -> R.drawable.ic_undo
    TransactionStatus.CANCELLED -> R.drawable.ic_block
    null -> R.drawable.ic_help
}

@Composable
fun TransactionStatus?.badgeColor(): Color = when (this) {
    TransactionStatus.REFUNDED -> MaterialTheme.colorScheme.secondary
    null -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> color()
}

@DrawableRes
fun TransactionDetailsError.icon(): Int = when (this) {
    TransactionDetailsError.NETWORK -> R.drawable.ic_cloud_off
    TransactionDetailsError.NOT_FOUND, TransactionDetailsError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun TransactionDetailsError.title(): Int = when (this) {
    TransactionDetailsError.NETWORK -> R.string.transactions_error_network_title
    TransactionDetailsError.NOT_FOUND -> R.string.transaction_details_not_found_title
    TransactionDetailsError.UNKNOWN -> R.string.transactions_error_unknown_title
}

@StringRes
fun TransactionDetailsError.body(): Int = when (this) {
    TransactionDetailsError.NETWORK -> R.string.transactions_error_network_body
    TransactionDetailsError.NOT_FOUND -> R.string.transaction_details_not_found_body
    TransactionDetailsError.UNKNOWN -> R.string.transactions_error_unknown_body
}
