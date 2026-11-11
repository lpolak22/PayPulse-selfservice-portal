package com.example.paypulse.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.SearchField

@Composable
fun TransactionsToolbar(
    searchText: String,
    activeFilterCount: Int,
    onEvent: (TransactionsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchField(
            query = searchText,
            onQueryChange = { onEvent(TransactionsEvent.SearchChanged(it)) },
            placeholder = stringResource(R.string.transactions_search_placeholder),
            modifier = Modifier.weight(1f),
            onSearch = { onEvent(TransactionsEvent.SearchSubmitted) },
            onClear = { onEvent(TransactionsEvent.SearchCleared) }
        )
        FilterButton(activeCount = activeFilterCount, onClick = { onEvent(TransactionsEvent.FilterButtonClicked) })
    }
}

@Composable
private fun FilterButton(activeCount: Int, onClick: () -> Unit) {
    val description = if (activeCount > 0) {
        stringResource(R.string.transactions_filters_active, activeCount)
    } else {
        stringResource(R.string.transactions_filters)
    }
    BadgedBox(
        badge = {
            if (activeCount > 0) {
                Badge(modifier = Modifier.clearAndSetSemantics { }) { Text(CroatianFormat.count(activeCount)) }
            }
        }
    ) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(painter = painterResource(R.drawable.ic_filter_list), contentDescription = description)
        }
    }
}
