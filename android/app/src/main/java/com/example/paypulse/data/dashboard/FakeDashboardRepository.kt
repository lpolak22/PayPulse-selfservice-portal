package com.example.paypulse.data.dashboard

import com.example.paypulse.domain.dashboard.DashboardRepository
import com.example.paypulse.domain.dashboard.DashboardResult
import com.example.paypulse.domain.dashboard.DateRange
import kotlinx.coroutines.delay
import java.time.Clock

private const val FAKE_DELAY_MILLIS = 500L

class FakeDashboardRepository(private val clock: Clock) : DashboardRepository {

    override suspend fun getSummary(range: DateRange): DashboardResult {
        delay(FAKE_DELAY_MILLIS)
        return DashboardResult.Success(fakeSummaryFor(range, clock.instant()))
    }
}
