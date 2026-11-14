package com.example.paypulse.domain.merchant

sealed interface MerchantResult {
    data class Success(val page: MerchantPage) : MerchantResult
    data class Failure(val error: MerchantError) : MerchantResult
}
