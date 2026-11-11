package com.example.paypulse.domain.transaction

sealed interface TransactionResult {
    data class Success(val page: TransactionPage) : TransactionResult
    data class Failure(val error: TransactionError) : TransactionResult
}
