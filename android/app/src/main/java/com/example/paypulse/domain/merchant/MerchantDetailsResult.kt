package com.example.paypulse.domain.merchant

sealed interface MerchantDetailsResult {
    data class Success(val details: MerchantDetails) : MerchantDetailsResult
    data class Failure(val error: MerchantDetailsError) : MerchantDetailsResult
}

enum class MerchantDetailsError { NOT_FOUND, NETWORK, UNKNOWN }
