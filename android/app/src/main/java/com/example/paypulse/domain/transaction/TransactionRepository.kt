package com.example.paypulse.domain.transaction

interface TransactionRepository {
    suspend fun search(query: TransactionQuery): TransactionResult
}
