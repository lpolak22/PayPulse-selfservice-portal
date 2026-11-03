package com.example.paypulse.ui.dashboard.charts

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardTrend
import com.example.paypulse.domain.dashboard.TrendPoint
import com.example.paypulse.ui.components.CroatianDateFormat
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.charts.BarChartEntry

private const val HOUR_LABEL_STEP = 6
private const val DATE_LABEL_STEP = 5

data class TrendChart(
    @get:StringRes val title: Int,
    val entries: List<BarChartEntry>,
    val details: List<ChartDetail>,
    val peakIndex: Int?,
    val lowestIndex: Int?
)

fun DashboardTrend.toTrendChart(period: DashboardPeriod): TrendChart {
    val title = if (period == DashboardPeriod.TODAY) R.string.dashboard_trend_title_hours else R.string.dashboard_trend_title_days
    return TrendChart(
        title = title,
        entries = points.mapIndexed { index, point -> point.toBarEntry(index, period) },
        details = points.map { point -> ChartDetail(point.detailLabel(period), point.amount, point.count) },
        peakIndex = points.indices.maxByOrNull { points[it].amount }?.takeIf { points[it].amount.signum() > 0 },
        lowestIndex = points.indices.minByOrNull { points[it].amount }
    )
}

private fun TrendPoint.toBarEntry(index: Int, period: DashboardPeriod): BarChartEntry {
    val date = start.toLocalDate()
    return when (period) {
        DashboardPeriod.TODAY -> BarChartEntry(
            value = amount.toFloat(),
            axisLabel = if (index % HOUR_LABEL_STEP == 0) CroatianDateFormat.hour(start) else null
        )
        DashboardPeriod.LAST_7_DAYS -> BarChartEntry(
            value = amount.toFloat(),
            axisLabel = CroatianDateFormat.weekdayShort(date),
            valueLabel = CroatianFormat.compactAmount(amount)
        )
        DashboardPeriod.LAST_30_DAYS -> BarChartEntry(
            value = amount.toFloat(),
            axisLabel = if (index % DATE_LABEL_STEP == 0) CroatianDateFormat.compactDate(date) else null
        )
    }
}

private fun TrendPoint.detailLabel(period: DashboardPeriod): String =
    if (period == DashboardPeriod.TODAY) CroatianDateFormat.hourRange(start)
    else CroatianDateFormat.weekdayWithDate(start.toLocalDate())
