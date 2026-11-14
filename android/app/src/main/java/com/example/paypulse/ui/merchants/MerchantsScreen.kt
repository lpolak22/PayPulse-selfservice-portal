package com.example.paypulse.ui.merchants

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
import androidx.compose.material3.FilterChip
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
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.components.SearchField
import com.example.paypulse.ui.components.StatusMessage

@Composable
fun MerchantsScreen(
    state: MerchantsUiState,
    onEvent: (MerchantsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PayPulseTopBar(onMenuClick = { onEvent(MerchantsEvent.MenuClicked) }) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MerchantsHeader(state = state, onEvent = onEvent)
            MerchantsBody(state = state, onEvent = onEvent, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MerchantsHeader(state: MerchantsUiState, onEvent: (MerchantsEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.merchants_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )
        SearchField(
            query = state.searchText,
            onQueryChange = { onEvent(MerchantsEvent.SearchChanged(it)) },
            placeholder = stringResource(R.string.merchants_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClear = { onEvent(MerchantsEvent.SearchCleared) }
        )
        StatusFilterRow(selected = state.status, onSelect = { onEvent(MerchantsEvent.StatusSelected(it)) })
        (state.content as? MerchantsContent.Loaded)?.let { ResultCount(total = it.total) }
    }
}

@Composable
private fun StatusFilterRow(selected: MerchantStatus?, onSelect: (MerchantStatus?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statusFilters.forEach { status ->
            FilterChip(
                selected = status == selected,
                onClick = { onSelect(status) },
                label = { Text(text = stringResource(status.filterLabel())) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}

@Composable
private fun ResultCount(total: Long) {
    val count = total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    Text(
        text = pluralStringResource(R.plurals.merchants_count, count, CroatianFormat.count(count)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun MerchantsBody(state: MerchantsUiState, onEvent: (MerchantsEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val content = state.content) {
            MerchantsContent.Loading -> MerchantsSkeleton()
            MerchantsContent.Empty -> EmptyMessage(hasFilters = state.hasFilters, onEvent = onEvent)
            is MerchantsContent.Error -> ErrorMessage(error = content.error, onEvent = onEvent)
            is MerchantsContent.Loaded -> MerchantsList(content = content, onEvent = onEvent)
        }
    }
}

@Composable
private fun EmptyMessage(hasFilters: Boolean, onEvent: (MerchantsEvent) -> Unit) {
    StatusMessage(
        icon = R.drawable.ic_inbox,
        title = stringResource(R.string.merchants_empty_title),
        body = stringResource(if (hasFilters) R.string.merchants_empty_body else R.string.merchants_empty_no_filters_body),
        modifier = Modifier.verticalScroll(rememberScrollState()),
        action = if (hasFilters) {
            { MessageButton(text = stringResource(R.string.merchants_clear_filters), onClick = { onEvent(MerchantsEvent.ClearFilters) }) }
        } else {
            null
        }
    )
}

@Composable
private fun ErrorMessage(error: MerchantError, onEvent: (MerchantsEvent) -> Unit) {
    val isFilterError = error == MerchantError.INVALID_FILTER
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(
            text = stringResource(if (isFilterError) R.string.merchants_clear_filters else R.string.merchants_retry),
            onClick = { onEvent(if (isFilterError) MerchantsEvent.ClearFilters else MerchantsEvent.Retry) }
        )
    }
}

@Composable
private fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text = text)
    }
}
