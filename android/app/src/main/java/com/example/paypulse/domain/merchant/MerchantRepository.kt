package com.example.paypulse.domain.merchant

interface MerchantRepository {
    suspend fun search(query: MerchantQuery): MerchantResult
}
