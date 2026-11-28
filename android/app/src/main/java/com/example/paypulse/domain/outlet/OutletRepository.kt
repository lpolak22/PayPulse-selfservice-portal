package com.example.paypulse.domain.outlet

interface OutletRepository {
    suspend fun search(query: OutletQuery): OutletResult

    suspend fun cities(merchantId: Long): OutletCitiesResult
}
