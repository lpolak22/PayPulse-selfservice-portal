package com.example.paypulse.ui.terminals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val LOAD_MORE_THRESHOLD = 3
private const val FOOTER_KEY = "paging-footer"

@Composable
fun TerminalsList(
    content: TerminalsContent.Loaded,
    onEvent: (TerminalsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LoadMoreEffect(listState = listState, paging = content.paging, onEvent = onEvent)
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = content.terminals, key = { it.id }) { terminal ->
            TerminalRow(terminal = terminal, onClick = { onEvent(TerminalsEvent.TerminalClicked(terminal.id)) })
        }
        item(key = FOOTER_KEY) {
            PagingFooter(paging = content.paging, onRetry = { onEvent(TerminalsEvent.Retry) })
        }
    }
}

@Composable
private fun LoadMoreEffect(listState: LazyListState, paging: TerminalsPaging, onEvent: (TerminalsEvent) -> Unit) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(listState, paging) {
        if (paging != TerminalsPaging.IDLE) return@LaunchedEffect
        snapshotFlow { listState.isNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnEvent(TerminalsEvent.LoadMore) }
    }
}

private fun LazyListState.isNearEnd(): Boolean {
    val info = layoutInfo
    val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
}

@Composable
private fun PagingFooter(paging: TerminalsPaging, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when (paging) {
            TerminalsPaging.LOADING_MORE -> LoadingMoreIndicator()
            TerminalsPaging.APPEND_ERROR -> AppendError(onRetry = onRetry)
            TerminalsPaging.IDLE, TerminalsPaging.END -> Unit
        }
    }
}

@Composable
private fun LoadingMoreIndicator() {
    val description = stringResource(R.string.terminals_loading_more)
    CircularProgressIndicator(
        modifier = Modifier
            .padding(8.dp)
            .size(32.dp)
            .semantics { contentDescription = description }
    )
}

@Composable
private fun AppendError(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.terminals_load_more_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(text = stringResource(R.string.terminals_retry))
        }
    }
}
