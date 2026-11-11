package com.example.paypulse.domain.dashboard

import java.math.BigDecimal
import java.time.LocalDateTime

enum class TrendUnit { HOUR, DAY }

data class TrendPoint(val start: LocalDateTime, val amount: BigDecimal, val count: Int)

data class DashboardTrend(val unit: TrendUnit, val points: List<TrendPoint>)
