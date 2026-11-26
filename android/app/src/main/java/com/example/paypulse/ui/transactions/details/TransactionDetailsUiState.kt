package com.example.paypulse.ui.transactions.details

import com.example.paypulse.domain.transaction.TransactionDetailsError

sealed interface TransactionDetailsUiState {
    data object Loading : TransactionDetailsUiState
    data object NotFound : TransactionDetailsUiState
    data class Error(val error: TransactionDetailsError) : TransactionDetailsUiState
    data class Loaded(val details: TransactionDetailsModel) : TransactionDetailsUiState
}
