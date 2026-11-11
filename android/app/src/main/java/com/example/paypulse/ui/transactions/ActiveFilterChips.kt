package com.example.paypulse.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R

@Composable
fun ActiveFilterChips(
    filters: List<ActiveFilter>,
    onRemove: (ActiveFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filters.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items = filters, key = { it.toString() }) { filter ->
            RemovableChip(label = activeFilterLabel(filter), onRemove = { onRemove(filter) })
        }
    }
}

@Composable
private fun RemovableChip(label: String, onRemove: () -> Unit) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.transactions_remove_filter, label),
                modifier = Modifier.size(InputChipDefaults.AvatarSize)
            )
        }
    )
}

@Composable
private fun activeFilterLabel(filter: ActiveFilter): String = when (filter) {
    is ActiveFilter.Dates -> stringResource(
        R.string.transactions_filter_dates,
        TransactionDateFormat.date(filter.range.from),
        TransactionDateFormat.date(filter.range.to)
    )
    is ActiveFilter.AuthCode -> stringResource(R.string.transactions_filter_auth_code, filter.value)
    is ActiveFilter.Rrn -> stringResource(R.string.transactions_filter_rrn, filter.value)
    is FilterOption -> stringResource(filter.label())
}
