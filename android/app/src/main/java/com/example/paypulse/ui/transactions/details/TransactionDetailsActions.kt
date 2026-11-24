package com.example.paypulse.ui.transactions.details

class TransactionDetailsActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    val onTerminalClick: (Long) -> Unit,
    val onOutletClick: (Long) -> Unit
)
