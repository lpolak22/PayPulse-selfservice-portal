package com.example.paypulse.ui.merchants.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MerchantDetailsNavigation(
    val onBackClick: () -> Unit,
    val onMerchantsClick: () -> Unit,
    val onOutletClick: (Long) -> Unit,
    val onTerminalClick: (Long) -> Unit
)

@Composable
fun MerchantDetailsRoute(
    navigation: MerchantDetailsNavigation,
    modifier: Modifier = Modifier,
    viewModel: MerchantDetailsViewModel = viewModel(factory = MerchantDetailsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentNavigation by rememberUpdatedState(navigation)
    val onEvent: (MerchantDetailsEvent) -> Unit = remember(viewModel) {
        { event ->
            when (event) {
                MerchantDetailsEvent.BackClicked -> currentNavigation.onBackClick()
                MerchantDetailsEvent.MerchantsClicked -> currentNavigation.onMerchantsClick()
                is MerchantDetailsEvent.OutletClicked -> currentNavigation.onOutletClick(event.id)
                is MerchantDetailsEvent.TerminalClicked -> currentNavigation.onTerminalClick(event.id)
                else -> viewModel.onEvent(event)
            }
        }
    }
    MerchantDetailsScreen(state = state, onEvent = onEvent, modifier = modifier)
}
