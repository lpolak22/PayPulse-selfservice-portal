package com.example.paypulse.ui.terminals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TerminalsRoute(
    onMenuClick: () -> Unit,
    onTerminalClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TerminalsViewModel = viewModel(factory = TerminalsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMenuClick by rememberUpdatedState(onMenuClick)
    val currentOnTerminalClick by rememberUpdatedState(onTerminalClick)
    val onEvent: (TerminalsEvent) -> Unit = remember(viewModel) {
        { event ->
            when (event) {
                TerminalsEvent.MenuClicked -> currentOnMenuClick()
                is TerminalsEvent.TerminalClicked -> currentOnTerminalClick(event.id)
                else -> viewModel.onEvent(event)
            }
        }
    }
    TerminalsScreen(state = state, onEvent = onEvent, modifier = modifier)
}
