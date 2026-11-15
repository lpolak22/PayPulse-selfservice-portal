package com.example.paypulse.ui.merchants

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantStatus

private const val BADGE_BACKGROUND_ALPHA = 0.14f

val statusFilters: List<MerchantStatus?> = listOf(null) + MerchantStatus.entries

@StringRes
fun MerchantStatus?.filterLabel(): Int = when (this) {
    null -> R.string.merchants_filter_all
    MerchantStatus.ACTIVE -> R.string.merchants_filter_active
    MerchantStatus.DEACTIVATED -> R.string.merchants_filter_deactivated
    MerchantStatus.HOLD -> R.string.merchants_filter_hold
}

@StringRes
fun MerchantStatus?.badgeLabel(): Int = when (this) {
    null -> R.string.merchants_status_unknown
    MerchantStatus.ACTIVE -> R.string.merchants_status_active
    MerchantStatus.DEACTIVATED -> R.string.merchants_status_deactivated
    MerchantStatus.HOLD -> R.string.merchants_status_hold
}

@Composable
fun MerchantStatus?.badgeColor(): Color = when (this) {
    MerchantStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
    MerchantStatus.HOLD -> MaterialTheme.colorScheme.secondary
    MerchantStatus.DEACTIVATED, null -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun MerchantStatus?.badgeBackground(): Color = badgeColor().copy(alpha = BADGE_BACKGROUND_ALPHA)

@DrawableRes
fun MerchantError.icon(): Int = when (this) {
    MerchantError.NETWORK -> R.drawable.ic_cloud_off
    MerchantError.INVALID_FILTER, MerchantError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun MerchantError.title(): Int = when (this) {
    MerchantError.NETWORK -> R.string.merchants_error_network_title
    MerchantError.INVALID_FILTER -> R.string.merchants_error_invalid_filter_title
    MerchantError.UNKNOWN -> R.string.merchants_error_unknown_title
}

@StringRes
fun MerchantError.body(): Int = when (this) {
    MerchantError.NETWORK -> R.string.merchants_error_network_body
    MerchantError.INVALID_FILTER -> R.string.merchants_error_invalid_filter_body
    MerchantError.UNKNOWN -> R.string.merchants_error_unknown_body
}
