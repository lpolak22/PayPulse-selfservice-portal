package com.example.paypulse.ui.transactions.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.ui.transactions.FilterOption
import com.example.paypulse.ui.transactions.label

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipGroup(
    options: List<FilterOption>,
    isSelected: (FilterOption) -> Boolean,
    onToggle: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onToggle(option) },
                label = { Text(stringResource(option.label())) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortSelector(selected: TransactionSort, onSelect: (TransactionSort) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionSort.entries.forEach { sort ->
            FilterChip(
                selected = sort == selected,
                onClick = { onSelect(sort) },
                label = { Text(stringResource(sort.label())) }
            )
        }
    }
}
