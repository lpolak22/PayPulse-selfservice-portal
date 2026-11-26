package com.example.paypulse.domain.transaction

interface TransactionDetailsRepository {
    suspend fun getDetails(id: Long): TransactionDetailsResult
}
