package com.example.paypulse.ui.terminals

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalsScreen(
    state: TerminalsUiState,
    onEvent: (TerminalsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PayPulseTopBar(onMenuClick = { onEvent(TerminalsEvent.MenuClicked) }) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(TerminalsEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TerminalsHeader(state = state, onEvent = onEvent)
                TerminalsBody(state = state, onEvent = onEvent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TerminalsHeader(state: TerminalsUiState, onEvent: (TerminalsEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.terminals_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )
        FilterChipRow(statusFilters, state.status, { stringResource(it.filterLabel()) }) {
            onEvent(TerminalsEvent.StatusSelected(it))
        }
        FilterChipRow(typeFilters, state.type, { it?.apiValue ?: stringResource(R.string.terminals_filter_all_types) }) {
            onEvent(TerminalsEvent.TypeSelected(it))
        }
        (state.content as? TerminalsContent.Loaded)?.let { ResultCount(shown = it.terminals.size, total = it.total) }
    }
}

@Composable
private fun <T> FilterChipRow(
    options: List<T?>,
    selected: T?,
    label: @Composable (T?) -> String,
    onSelect: (T?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(text = label(option)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}

@Composable
private fun ResultCount(shown: Int, total: Long) {
    val totalText = CroatianFormat.count(total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    Text(
        text = stringResource(R.string.terminals_count, CroatianFormat.count(shown), totalText),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun TerminalsBody(state: TerminalsUiState, onEvent: (TerminalsEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val content = state.content) {
            TerminalsContent.Loading -> TerminalsSkeleton()
            TerminalsContent.Empty -> EmptyMessage(hasFilters = state.hasFilters, onEvent = onEvent)
            is TerminalsContent.Error -> ErrorMessage(error = content.error, onEvent = onEvent)
            is TerminalsContent.Loaded -> TerminalsList(content = content, onEvent = onEvent)
        }
    }
}

@Composable
private fun EmptyMessage(hasFilters: Boolean, onEvent: (TerminalsEvent) -> Unit) {
    StatusMessage(
        icon = R.drawable.ic_inbox,
        title = stringResource(R.string.terminals_empty_title),
        body = stringResource(if (hasFilters) R.string.terminals_empty_body else R.string.terminals_empty_no_filters_body),
        modifier = Modifier.verticalScroll(rememberScrollState()),
        action = if (hasFilters) {
            { MessageButton(text = stringResource(R.string.terminals_clear_filters), onClick = { onEvent(TerminalsEvent.ClearFilters) }) }
        } else {
            null
        }
    )
}

@Composable
private fun ErrorMessage(error: TerminalError, onEvent: (TerminalsEvent) -> Unit) {
    val isFilterError = error == TerminalError.INVALID_FILTER
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(
            text = stringResource(if (isFilterError) R.string.terminals_clear_filters else R.string.terminals_retry),
            onClick = { onEvent(if (isFilterError) TerminalsEvent.ClearFilters else TerminalsEvent.Retry) }
        )
    }
}

@Composable
private fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text = text)
    }
}
