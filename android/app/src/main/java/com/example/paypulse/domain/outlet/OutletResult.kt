package com.example.paypulse.domain.outlet

sealed interface OutletResult {
    data class Success(val page: OutletPage) : OutletResult
    data class Failure(val error: OutletError) : OutletResult
}
