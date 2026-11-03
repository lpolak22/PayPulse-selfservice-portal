package com.example.paypulse.ui.dashboard.charts

import com.example.paypulse.domain.dashboard.AcquirerTotal
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.charts.HorizontalBarItem

data class AcquirerChart(val items: List<HorizontalBarItem>, val details: List<ChartDetail>)

fun List<AcquirerTotal>.toAcquirerChart(): AcquirerChart {
    val sorted = sortedByDescending { it.amount }
    return AcquirerChart(
        items = sorted.map { HorizontalBarItem(it.acquirer, CroatianFormat.money(it.amount), it.amount.toFloat()) },
        details = sorted.map { ChartDetail(it.acquirer, it.amount, it.count) }
    )
}
