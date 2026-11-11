package com.example.paypulse.ui.transactions

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val LOAD_MORE_THRESHOLD = 5
private const val FOOTER_KEY = "paging-footer"

@Composable
fun TransactionsList(
    content: TransactionsContent.Loaded,
    onEvent: (TransactionsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LoadMoreEffect(listState = listState, paging = content.paging, onEvent = onEvent)
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(items = content.items, key = { it.key }, contentType = { it is TransactionListItem.DayHeader }) { item ->
            when (item) {
                is TransactionListItem.DayHeader -> DayHeader(label = item.label)
                is TransactionListItem.Row -> TransactionRow(
                    row = item.row,
                    onClick = { onEvent(TransactionsEvent.TransactionClicked(item.row.id)) }
                )
            }
        }
        item(key = FOOTER_KEY) {
            PagingFooter(paging = content.paging, onRetry = { onEvent(TransactionsEvent.Retry) })
        }
    }
}

@Composable
private fun LoadMoreEffect(listState: LazyListState, paging: PagingStatus, onEvent: (TransactionsEvent) -> Unit) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(listState, paging) {
        if (paging != PagingStatus.IDLE) return@LaunchedEffect
        snapshotFlow { listState.isNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnEvent(TransactionsEvent.LoadMore) }
    }
}

private fun LazyListState.isNearEnd(): Boolean {
    val info = layoutInfo
    val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
}

@Composable
private fun DayHeader(label: DayLabel) {
    val text = when (label) {
        DayLabel.Today -> stringResource(R.string.transactions_day_today)
        DayLabel.Yesterday -> stringResource(R.string.transactions_day_yesterday)
        is DayLabel.Date -> label.text
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun PagingFooter(paging: PagingStatus, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (paging) {
            PagingStatus.LOADING_MORE -> LoadingMoreIndicator()
            PagingStatus.APPEND_ERROR -> AppendError(onRetry = onRetry)
            PagingStatus.IDLE, PagingStatus.END -> Unit
        }
    }
}

@Composable
private fun LoadingMoreIndicator() {
    val description = stringResource(R.string.transactions_loading_more)
    CircularProgressIndicator(
        modifier = Modifier
            .size(32.dp)
            .semantics { contentDescription = description }
    )
}

@Composable
private fun AppendError(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.transactions_load_more_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(text = stringResource(R.string.transactions_retry))
        }
    }
}
