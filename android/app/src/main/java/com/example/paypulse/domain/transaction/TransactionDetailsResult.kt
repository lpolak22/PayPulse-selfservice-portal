package com.example.paypulse.domain.transaction

sealed interface TransactionDetailsResult {
    data class Success(val details: TransactionDetails) : TransactionDetailsResult
    data class Failure(val error: TransactionDetailsError) : TransactionDetailsResult
}
