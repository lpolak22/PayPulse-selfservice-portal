package com.example.paypulse.ui.transactions.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TransactionDetailsRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onTerminalClick: (Long) -> Unit,
    onOutletClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailsViewModel = viewModel(factory = TransactionDetailsViewModel.factory(transactionId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBackClick by rememberUpdatedState(onBackClick)
    val currentOnTerminalClick by rememberUpdatedState(onTerminalClick)
    val currentOnOutletClick by rememberUpdatedState(onOutletClick)
    val actions = remember(viewModel) {
        TransactionDetailsActions(
            onBackClick = { currentOnBackClick() },
            onRetry = viewModel::onRetry,
            onTerminalClick = { currentOnTerminalClick(it) },
            onOutletClick = { currentOnOutletClick(it) }
        )
    }
    TransactionDetailsScreen(state = state, actions = actions, modifier = modifier)
}
