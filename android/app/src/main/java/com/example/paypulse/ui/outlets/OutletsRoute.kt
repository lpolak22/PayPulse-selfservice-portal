package com.example.paypulse.ui.outlets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OutletsRoute(
    onBack: () -> Unit,
    onOutletClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OutletsViewModel = viewModel(factory = OutletsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnOutletClick by rememberUpdatedState(onOutletClick)
    val onEvent: (OutletsEvent) -> Unit = remember(viewModel) {
        { event ->
            when (event) {
                OutletsEvent.BackClicked -> currentOnBack()
                is OutletsEvent.OutletClicked -> currentOnOutletClick(event.id)
                else -> viewModel.onEvent(event)
            }
        }
    }
    OutletsScreen(state = state, onEvent = onEvent, modifier = modifier)
}
