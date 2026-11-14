package com.example.paypulse.ui.merchants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MerchantsRoute(
    onMenuClick: () -> Unit,
    onMerchantClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MerchantsViewModel = viewModel(factory = MerchantsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMenuClick by rememberUpdatedState(onMenuClick)
    val currentOnMerchantClick by rememberUpdatedState(onMerchantClick)
    val onEvent: (MerchantsEvent) -> Unit = remember(viewModel) {
        { event ->
            when (event) {
                MerchantsEvent.MenuClicked -> currentOnMenuClick()
                is MerchantsEvent.MerchantClicked -> currentOnMerchantClick(event.id)
                else -> viewModel.onEvent(event)
            }
        }
    }
    MerchantsScreen(state = state, onEvent = onEvent, modifier = modifier)
}
