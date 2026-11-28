package com.example.paypulse.domain.outlet

sealed interface OutletCitiesResult {
    data class Success(val cities: List<String>) : OutletCitiesResult
    data class Failure(val error: OutletError) : OutletCitiesResult
}
