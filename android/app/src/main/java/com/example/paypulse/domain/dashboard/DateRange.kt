package com.example.paypulse.domain.dashboard

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val from: LocalDate, val to: LocalDate) {
    val dayCount: Int
        get() = ChronoUnit.DAYS.between(from, to).toInt() + 1
}
