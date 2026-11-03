package com.example.paypulse.domain.dashboard

interface DashboardRepository {
    suspend fun getSummary(range: DateRange): DashboardResult
}
