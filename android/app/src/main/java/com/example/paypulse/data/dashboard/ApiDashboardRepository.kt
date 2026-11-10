package com.example.paypulse.data.dashboard

import com.example.paypulse.data.remote.DashboardApi
import com.example.paypulse.data.remote.mapper.toDashboardSummary
import com.example.paypulse.domain.dashboard.DashboardError
import com.example.paypulse.domain.dashboard.DashboardRepository
import com.example.paypulse.domain.dashboard.DashboardResult
import com.example.paypulse.domain.dashboard.DateRange
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApiDashboardRepository(private val dashboardApi: DashboardApi) : DashboardRepository {

    override suspend fun getSummary(range: DateRange): DashboardResult = try {
        val response = dashboardApi.summary(range.from.toQueryDate(), range.to.toQueryDate())
        DashboardResult.Success(response.toDashboardSummary())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        DashboardResult.Failure(DashboardError.NETWORK)
    } catch (exception: Exception) {
        DashboardResult.Failure(DashboardError.UNKNOWN)
    }

    private fun LocalDate.toQueryDate(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
}
