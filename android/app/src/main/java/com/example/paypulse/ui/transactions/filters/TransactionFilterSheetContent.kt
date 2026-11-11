package com.example.paypulse.ui.transactions.filters

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.ui.transactions.FilterDraft
import com.example.paypulse.ui.transactions.FilterOption
import com.example.paypulse.ui.transactions.FilterSheetEvent
import com.example.paypulse.ui.transactions.brandOptions
import com.example.paypulse.ui.transactions.channelOptions
import com.example.paypulse.ui.transactions.isSelected
import com.example.paypulse.ui.transactions.statusOptions
import com.example.paypulse.ui.transactions.typeOptions

@Composable
fun TransactionFilterSheetContent(
    draft: FilterDraft,
    onEvent: (FilterSheetEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SheetHeader(onClearAll = { onEvent(FilterSheetEvent.ClearAll) })
        PeriodAndSortSections(draft = draft, onEvent = onEvent)
        OptionSection(R.string.transactions_section_status, statusOptions, draft.filters, onEvent)
        OptionSection(R.string.transactions_section_type, typeOptions, draft.filters, onEvent)
        OptionSection(R.string.transactions_section_brand, brandOptions, draft.filters, onEvent)
        OptionSection(R.string.transactions_section_channel, channelOptions, draft.filters, onEvent)
        CodeFields(filters = draft.filters, onEvent = onEvent)
        Button(onClick = { onEvent(FilterSheetEvent.Apply) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(text = stringResource(R.string.transactions_show_results))
        }
    }
}

@Composable
private fun SheetHeader(onClearAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.transactions_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        TextButton(onClick = onClearAll, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(text = stringResource(R.string.transactions_clear_all))
        }
    }
}

@Composable
private fun PeriodAndSortSections(draft: FilterDraft, onEvent: (FilterSheetEvent) -> Unit) {
    FilterSection(R.string.transactions_section_period) {
        DateRangeField(
            range = draft.filters.dateRange,
            today = draft.today,
            onRangeChange = { onEvent(FilterSheetEvent.DatesChanged(it)) }
        )
    }
    FilterSection(R.string.transactions_section_sort) {
        SortSelector(selected = draft.sort, onSelect = { onEvent(FilterSheetEvent.SortChanged(it)) })
    }
}

@Composable
private fun FilterSection(@StringRes title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Composable
private fun OptionSection(
    @StringRes title: Int,
    options: List<FilterOption>,
    filters: TransactionFilters,
    onEvent: (FilterSheetEvent) -> Unit
) {
    FilterSection(title) {
        FilterChipGroup(
            options = options,
            isSelected = filters::isSelected,
            onToggle = { onEvent(FilterSheetEvent.OptionToggled(it)) }
        )
    }
}

@Composable
private fun CodeFields(filters: TransactionFilters, onEvent: (FilterSheetEvent) -> Unit) {
    OutlinedTextField(
        value = filters.authCode.orEmpty(),
        onValueChange = { onEvent(FilterSheetEvent.AuthCodeChanged(it)) },
        label = { Text(stringResource(R.string.transactions_auth_code)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = filters.rrn.orEmpty(),
        onValueChange = { onEvent(FilterSheetEvent.RrnChanged(it)) },
        label = { Text(stringResource(R.string.transactions_rrn)) },
        supportingText = { Text(stringResource(R.string.transactions_rrn_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth()
    )
}
