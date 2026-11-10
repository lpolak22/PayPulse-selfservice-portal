package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.DashboardSummaryDto
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {
    @GET("api/dashboard/summary")
    suspend fun summary(@Query("from") from: String, @Query("to") to: String): DashboardSummaryDto
}
