package com.example.paypulse.ui.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TransactionsRoute(
    onMenuClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel(factory = TransactionsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMenuClick by rememberUpdatedState(onMenuClick)
    val currentOnTransactionClick by rememberUpdatedState(onTransactionClick)
    val onEvent: (TransactionsEvent) -> Unit = remember(viewModel) {
        { event ->
            when (event) {
                TransactionsEvent.MenuClicked -> currentOnMenuClick()
                is TransactionsEvent.TransactionClicked -> currentOnTransactionClick(event.id)
                else -> viewModel.onEvent(event)
            }
        }
    }
    TransactionsScreen(state = state, onEvent = onEvent, modifier = modifier)
}
