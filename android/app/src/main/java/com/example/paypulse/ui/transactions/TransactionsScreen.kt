package com.example.paypulse.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.components.StatusMessage
import com.example.paypulse.ui.transactions.filters.TransactionFilterSheet

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onEvent: (TransactionsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PayPulseTopBar(onMenuClick = { onEvent(TransactionsEvent.MenuClicked) }) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TransactionsHeader(state = state, onEvent = onEvent)
            TransactionsBody(content = state.content, onEvent = onEvent, modifier = Modifier.weight(1f))
        }
    }
    state.filterDraft?.let { TransactionFilterSheet(draft = it, onEvent = onEvent) }
}

@Composable
private fun TransactionsHeader(state: TransactionsUiState, onEvent: (TransactionsEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.transactions_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )
        TransactionsToolbar(
            searchText = state.searchText,
            activeFilterCount = state.activeFilterCount,
            onEvent = onEvent,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ActiveFilterChips(filters = state.activeFilters, onRemove = { onEvent(TransactionsEvent.ActiveFilterRemoved(it)) })
        (state.content as? TransactionsContent.Loaded)?.let { ResultCount(total = it.total) }
    }
}

@Composable
private fun ResultCount(total: Long) {
    val count = total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    Text(
        text = pluralStringResource(R.plurals.transactions_count, count, CroatianFormat.count(count)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun TransactionsBody(content: TransactionsContent, onEvent: (TransactionsEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (content) {
            TransactionsContent.Loading -> TransactionsSkeleton()
            TransactionsContent.Empty -> EmptyMessage(onClearFilters = { onEvent(TransactionsEvent.ClearFilters) })
            is TransactionsContent.Error -> ErrorMessage(error = content.error, onEvent = onEvent)
            is TransactionsContent.Loaded -> TransactionsList(content = content, onEvent = onEvent)
        }
    }
}

@Composable
private fun EmptyMessage(onClearFilters: () -> Unit) {
    StatusMessage(
        icon = R.drawable.ic_inbox,
        title = stringResource(R.string.transactions_empty_title),
        body = stringResource(R.string.transactions_empty_body),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(text = stringResource(R.string.transactions_clear_filters), onClick = onClearFilters)
    }
}

@Composable
private fun ErrorMessage(error: TransactionError, onEvent: (TransactionsEvent) -> Unit) {
    val isFilterError = error == TransactionError.INVALID_FILTER
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(
            text = stringResource(if (isFilterError) R.string.transactions_clear_filters else R.string.transactions_retry),
            onClick = { onEvent(if (isFilterError) TransactionsEvent.ClearFilters else TransactionsEvent.Retry) }
        )
    }
}

@Composable
private fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text = text)
    }
}
