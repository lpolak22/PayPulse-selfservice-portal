package com.example.paypulse.ui.transactions.filters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.paypulse.ui.transactions.FilterDraft
import com.example.paypulse.ui.transactions.FilterSheetEvent
import com.example.paypulse.ui.transactions.TransactionsEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterSheet(draft: FilterDraft, onEvent: (TransactionsEvent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = { onEvent(FilterSheetEvent.Dismissed) }, sheetState = sheetState) {
        TransactionFilterSheetContent(
            draft = draft,
            onEvent = { event ->
                if (event == FilterSheetEvent.Apply) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onEvent(event) }
                } else {
                    onEvent(event)
                }
            }
        )
    }
}
