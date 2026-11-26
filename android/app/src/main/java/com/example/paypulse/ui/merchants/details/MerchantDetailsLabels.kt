package com.example.paypulse.ui.merchants.details

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus

private const val MAX_INITIALS = 2

fun merchantInitials(name: String): String {
    val words = name.split(' ').filter { word -> word.firstOrNull()?.isLetterOrDigit() == true && '.' !in word }
    val initials = words.take(MAX_INITIALS).joinToString("") { it.first().toString() }
    return initials.ifEmpty { name.trim().take(1) }.uppercase()
}

fun Outlet.addressLine(): String? {
    val place = listOfNotNull(postalCode, city).joinToString(" ").ifEmpty { null }
    return listOfNotNull(address, place).joinToString(", ").ifEmpty { null }
}

@DrawableRes
fun MerchantDetailsError.icon(): Int = when (this) {
    MerchantDetailsError.NOT_FOUND -> R.drawable.ic_store
    MerchantDetailsError.NETWORK -> R.drawable.ic_cloud_off
    MerchantDetailsError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun MerchantDetailsError.title(): Int = when (this) {
    MerchantDetailsError.NOT_FOUND -> R.string.merchant_details_not_found_title
    MerchantDetailsError.NETWORK -> R.string.merchants_error_network_title
    MerchantDetailsError.UNKNOWN -> R.string.merchants_error_unknown_title
}

@StringRes
fun MerchantDetailsError.body(): Int = when (this) {
    MerchantDetailsError.NOT_FOUND -> R.string.merchant_details_not_found_body
    MerchantDetailsError.NETWORK -> R.string.merchants_error_network_body
    MerchantDetailsError.UNKNOWN -> R.string.merchants_error_unknown_body
}

@StringRes
fun OutletStatus?.label(): Int = when (this) {
    OutletStatus.ACTIVE -> R.string.outlet_status_active
    OutletStatus.DEACTIVATED -> R.string.outlet_status_deactivated
    OutletStatus.HOLD -> R.string.outlet_status_hold
    null -> R.string.merchants_status_unknown
}

@Composable
fun OutletStatus?.color(): Color = when (this) {
    OutletStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
    OutletStatus.HOLD -> MaterialTheme.colorScheme.secondary
    OutletStatus.DEACTIVATED, null -> MaterialTheme.colorScheme.outline
}

@StringRes
fun TerminalStatus?.label(): Int = when (this) {
    TerminalStatus.PREPARED -> R.string.terminal_status_prepared
    TerminalStatus.AUTHENTICATED -> R.string.terminal_status_authenticated
    TerminalStatus.ACTIVE -> R.string.terminal_status_active
    TerminalStatus.INACTIVE -> R.string.terminal_status_inactive
    TerminalStatus.DEACTIVATED -> R.string.terminal_status_deactivated
    TerminalStatus.SUSPENDED -> R.string.terminal_status_suspended
    TerminalStatus.BLOCKED -> R.string.terminal_status_blocked
    TerminalStatus.UNKNOWN, null -> R.string.merchants_status_unknown
}

@Composable
fun TerminalStatus?.color(): Color = when (this) {
    TerminalStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
    TerminalStatus.PREPARED, TerminalStatus.AUTHENTICATED -> MaterialTheme.colorScheme.secondary
    TerminalStatus.SUSPENDED, TerminalStatus.BLOCKED -> MaterialTheme.colorScheme.error
    TerminalStatus.INACTIVE, TerminalStatus.DEACTIVATED, TerminalStatus.UNKNOWN, null ->
        MaterialTheme.colorScheme.outline
}
