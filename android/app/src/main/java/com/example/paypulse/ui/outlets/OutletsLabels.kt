package com.example.paypulse.ui.outlets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paypulse.R
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletStatus

private const val BADGE_BACKGROUND_ALPHA = 0.14f

@StringRes
fun OutletStatus?.badgeLabel(): Int = when (this) {
    null -> R.string.outlets_status_unknown
    OutletStatus.ACTIVE -> R.string.outlets_status_active
    OutletStatus.DEACTIVATED -> R.string.outlets_status_deactivated
    OutletStatus.HOLD -> R.string.outlets_status_hold
}

@Composable
fun OutletStatus?.badgeColor(): Color = when (this) {
    OutletStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
    OutletStatus.HOLD -> MaterialTheme.colorScheme.secondary
    OutletStatus.DEACTIVATED, null -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun OutletStatus?.badgeBackground(): Color = badgeColor().copy(alpha = BADGE_BACKGROUND_ALPHA)

@DrawableRes
fun OutletError.icon(): Int = when (this) {
    OutletError.NETWORK -> R.drawable.ic_cloud_off
    OutletError.MERCHANT_NOT_FOUND, OutletError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun OutletError.title(): Int = when (this) {
    OutletError.NETWORK -> R.string.outlets_error_network_title
    OutletError.MERCHANT_NOT_FOUND -> R.string.outlets_error_not_found_title
    OutletError.UNKNOWN -> R.string.outlets_error_unknown_title
}

@StringRes
fun OutletError.body(): Int = when (this) {
    OutletError.NETWORK -> R.string.outlets_error_network_body
    OutletError.MERCHANT_NOT_FOUND -> R.string.outlets_error_not_found_body
    OutletError.UNKNOWN -> R.string.outlets_error_unknown_body
}
