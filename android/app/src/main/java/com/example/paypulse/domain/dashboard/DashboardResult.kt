package com.example.paypulse.domain.dashboard

sealed interface DashboardResult {
    data class Success(val summary: DashboardSummary) : DashboardResult
    data class Failure(val error: DashboardError) : DashboardResult
}
