package com.example.paypulse.domain.dashboard

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

enum class DashboardPeriod(private val days: Long) {
    TODAY(1),
    LAST_7_DAYS(7),
    LAST_30_DAYS(30);

    fun toDateRange(clock: Clock): DateRange {
        val today = LocalDate.now(clock.withZone(ZONE))
        return DateRange(from = today.minusDays(days - 1), to = today)
    }

    companion object {
        val ZONE: ZoneId = ZoneId.of("Europe/Zagreb")
        val DEFAULT = LAST_7_DAYS
    }
}
