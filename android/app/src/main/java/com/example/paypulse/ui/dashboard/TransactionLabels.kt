package com.example.paypulse.ui.dashboard

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus

@StringRes
fun CardBrand.label(): Int = when (this) {
    CardBrand.VISA -> R.string.dashboard_brand_visa
    CardBrand.MASTERCARD -> R.string.dashboard_brand_mastercard
    CardBrand.AMEX -> R.string.dashboard_brand_amex
    CardBrand.DINERS -> R.string.dashboard_brand_diners
    CardBrand.MAESTRO -> R.string.dashboard_brand_maestro
}

@StringRes
fun TransactionStatus.label(): Int = when (this) {
    TransactionStatus.APPROVED -> R.string.dashboard_status_approved
    TransactionStatus.DECLINED -> R.string.dashboard_status_declined
    TransactionStatus.REFUNDED -> R.string.dashboard_status_refunded
    TransactionStatus.CANCELLED -> R.string.dashboard_status_cancelled
}

@Composable
fun TransactionStatus.color(): Color = when (this) {
    TransactionStatus.APPROVED -> MaterialTheme.colorScheme.tertiary
    TransactionStatus.DECLINED -> MaterialTheme.colorScheme.error
    TransactionStatus.REFUNDED, TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
}
