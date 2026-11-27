package com.example.paypulse.ui.outlets

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.SearchField
import com.example.paypulse.ui.components.StatusMessage

@Composable
fun OutletsScreen(
    state: OutletsUiState,
    onEvent: (OutletsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { OutletsTopBar(merchantName = state.merchantName, onBack = { onEvent(OutletsEvent.BackClicked) }) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        OutletsPane(state = state, onEvent = onEvent, modifier = Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutletsTopBar(merchantName: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = merchantName.ifBlank { stringResource(R.string.outlets_title) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.outlets_back)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletsPane(
    state: OutletsUiState,
    onEvent: (OutletsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(OutletsEvent.Refresh) },
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutletsHeader(state = state, onEvent = onEvent)
            OutletsBody(state = state, onEvent = onEvent, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun OutletsHeader(state: OutletsUiState, onEvent: (OutletsEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderTitle()
        SearchField(
            query = state.searchText,
            onQueryChange = { onEvent(OutletsEvent.SearchChanged(it)) },
            placeholder = stringResource(R.string.outlets_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClear = { onEvent(OutletsEvent.SearchCleared) }
        )
        CityFilter(
            selected = state.city,
            cities = state.cities,
            onSelect = { onEvent(OutletsEvent.CitySelected(it)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        (state.content as? OutletsContent.Loaded)?.let { ResultCount(total = it.total) }
    }
}

@Composable
private fun HeaderTitle() {
    Text(
        text = stringResource(R.string.outlets_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .semantics { heading() }
    )
}

@Composable
private fun ResultCount(total: Long) {
    val count = CroatianFormat.count(total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    Text(
        text = stringResource(R.string.outlets_count, count),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun OutletsBody(state: OutletsUiState, onEvent: (OutletsEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val content = state.content) {
            OutletsContent.Loading -> OutletsSkeleton()
            OutletsContent.Empty -> EmptyMessage(hasFilters = state.hasFilters, onEvent = onEvent)
            is OutletsContent.Error -> ErrorMessage(error = content.error, onEvent = onEvent)
            is OutletsContent.Loaded -> OutletsList(content = content, onEvent = onEvent)
        }
    }
}

@Composable
private fun EmptyMessage(hasFilters: Boolean, onEvent: (OutletsEvent) -> Unit) {
    StatusMessage(
        icon = R.drawable.ic_inbox,
        title = stringResource(R.string.outlets_empty_title),
        body = stringResource(if (hasFilters) R.string.outlets_empty_body else R.string.outlets_empty_no_filters_body),
        modifier = Modifier.verticalScroll(rememberScrollState()),
        action = if (hasFilters) {
            { MessageButton(text = stringResource(R.string.outlets_clear_filters), onClick = { onEvent(OutletsEvent.ClearFilters) }) }
        } else {
            null
        }
    )
}

@Composable
private fun ErrorMessage(error: OutletError, onEvent: (OutletsEvent) -> Unit) {
    val isNotFound = error == OutletError.MERCHANT_NOT_FOUND
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(
            text = stringResource(if (isNotFound) R.string.outlets_back else R.string.outlets_retry),
            onClick = { onEvent(if (isNotFound) OutletsEvent.BackClicked else OutletsEvent.Retry) }
        )
    }
}

@Composable
private fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text = text)
    }
}
