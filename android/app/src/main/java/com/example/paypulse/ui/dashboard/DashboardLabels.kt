package com.example.paypulse.ui.dashboard

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardPeriod

@StringRes
fun DashboardPeriod.label(): Int = when (this) {
    DashboardPeriod.TODAY -> R.string.dashboard_period_today
    DashboardPeriod.LAST_7_DAYS -> R.string.dashboard_period_last_7_days
    DashboardPeriod.LAST_30_DAYS -> R.string.dashboard_period_last_30_days
}

@StringRes
fun DashboardPeriod.comparisonLabel(): Int = when (this) {
    DashboardPeriod.TODAY -> R.string.dashboard_change_vs_yesterday
    DashboardPeriod.LAST_7_DAYS -> R.string.dashboard_change_vs_last_week
    DashboardPeriod.LAST_30_DAYS -> R.string.dashboard_change_vs_previous_30_days
}

@DrawableRes
fun DashboardError.icon(): Int = when (this) {
    DashboardError.NETWORK -> R.drawable.ic_cloud_off
    DashboardError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun DashboardError.title(): Int = when (this) {
    DashboardError.NETWORK -> R.string.dashboard_error_network_title
    DashboardError.UNKNOWN -> R.string.dashboard_error_unknown_title
}

@StringRes
fun DashboardError.body(): Int = when (this) {
    DashboardError.NETWORK -> R.string.dashboard_error_network_body
    DashboardError.UNKNOWN -> R.string.dashboard_error_unknown_body
}

@StringRes
fun DashboardError.bannerMessage(): Int = when (this) {
    DashboardError.NETWORK -> R.string.dashboard_banner_network
    DashboardError.UNKNOWN -> R.string.dashboard_banner_unknown
}
