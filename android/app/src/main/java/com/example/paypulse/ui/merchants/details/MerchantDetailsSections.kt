package com.example.paypulse.ui.merchants.details

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.components.SearchField
import com.example.paypulse.ui.components.StatusMessage

@Composable
fun MerchantDetailsTabs(
    details: MerchantDetails,
    selected: MerchantDetailsTab,
    onSelect: (MerchantDetailsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(selectedTabIndex = selected.ordinal, containerColor = Color.Transparent, modifier = modifier) {
        MerchantDetailsTab.entries.forEach { tab ->
            val total = when (tab) {
                MerchantDetailsTab.OUTLETS -> details.outlets.total
                MerchantDetailsTab.TERMINALS -> details.terminals.total
            }
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { TabLabel(label = tab.label(), total = total, isSelected = tab == selected) }
            )
        }
    }
}

@Composable
private fun TabLabel(@StringRes label: Int, total: Long, isSelected: Boolean) {
    Text(
        text = stringResource(label, formatCount(total)),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
    )
}

fun LazyListScope.relatedItemsSection(state: MerchantDetailsUiState, onEvent: (MerchantDetailsEvent) -> Unit) {
    val details = state.details ?: return
    val search = state.currentSearch
    when (state.selectedTab) {
        MerchantDetailsTab.OUTLETS -> {
            section(state.selectedTab, details.outlets, state.visibleOutlets, search, onEvent) {
                items(items = it, key = { outlet -> "outlet-${outlet.id}" }) { outlet ->
                    OutletRow(outlet = outlet, onClick = { onEvent(MerchantDetailsEvent.OutletClicked(outlet.id)) })
                }
            }
            allOutletsButton(details, onEvent)
        }
        MerchantDetailsTab.TERMINALS -> section(state.selectedTab, details.terminals, state.visibleTerminals, search, onEvent) {
            items(items = it, key = { terminal -> "terminal-${terminal.id}" }) { terminal ->
                TerminalRow(terminal = terminal, onClick = { onEvent(MerchantDetailsEvent.TerminalClicked(terminal.id)) })
            }
        }
    }
}

private fun <T> LazyListScope.section(
    tab: MerchantDetailsTab,
    all: RelatedItems<T>,
    visible: List<T>,
    search: String,
    onEvent: (MerchantDetailsEvent) -> Unit,
    rows: LazyListScope.(List<T>) -> Unit
) {
    if (all.items.isEmpty()) {
        item(key = "empty") { NoItemsMessage(tab = tab) }
        return
    }
    item(key = "search") { RelatedSearchField(tab = tab, search = search, onEvent = onEvent) }
    if (visible.isEmpty()) {
        item(key = "no-results") { NoResultsMessage(search = search.trim()) }
    } else {
        rows(visible)
    }
    if (all.total > all.items.size && search.isBlank()) {
        item(key = "shown-count") { ShownCount(shown = all.items.size.toLong(), total = all.total) }
    }
}

private fun LazyListScope.allOutletsButton(details: MerchantDetails, onEvent: (MerchantDetailsEvent) -> Unit) {
    if (details.outlets.items.isEmpty()) return
    item(key = "all-outlets") {
        OutlinedButton(
            onClick = { onEvent(MerchantDetailsEvent.AllOutletsClicked(details.id, details.name)) },
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.merchant_details_all_outlets))
        }
    }
}

@Composable
private fun RelatedSearchField(tab: MerchantDetailsTab, search: String, onEvent: (MerchantDetailsEvent) -> Unit) {
    SearchField(
        query = search,
        onQueryChange = { onEvent(MerchantDetailsEvent.SearchChanged(it)) },
        placeholder = stringResource(tab.searchPlaceholder()),
        onClear = { onEvent(MerchantDetailsEvent.SearchCleared) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun NoItemsMessage(tab: MerchantDetailsTab) {
    StatusMessage(
        icon = tab.emptyIcon(),
        title = stringResource(tab.emptyTitle()),
        body = stringResource(tab.emptyBody())
    )
}

@Composable
private fun NoResultsMessage(search: String) {
    StatusMessage(
        icon = R.drawable.ic_search,
        title = stringResource(R.string.merchant_details_no_results_title),
        body = stringResource(R.string.merchant_details_no_results_body, search)
    )
}

@Composable
private fun ShownCount(shown: Long, total: Long) {
    Text(
        text = stringResource(R.string.merchant_details_shown_count, formatCount(shown), formatCount(total)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@StringRes
private fun MerchantDetailsTab.label(): Int = when (this) {
    MerchantDetailsTab.OUTLETS -> R.string.merchant_details_outlets_tab
    MerchantDetailsTab.TERMINALS -> R.string.merchant_details_terminals_tab
}

@StringRes
private fun MerchantDetailsTab.searchPlaceholder(): Int = when (this) {
    MerchantDetailsTab.OUTLETS -> R.string.merchant_details_outlets_search
    MerchantDetailsTab.TERMINALS -> R.string.merchant_details_terminals_search
}

@DrawableRes
private fun MerchantDetailsTab.emptyIcon(): Int = when (this) {
    MerchantDetailsTab.OUTLETS -> R.drawable.ic_store
    MerchantDetailsTab.TERMINALS -> R.drawable.ic_point_of_sale
}

@StringRes
private fun MerchantDetailsTab.emptyTitle(): Int = when (this) {
    MerchantDetailsTab.OUTLETS -> R.string.merchant_details_no_outlets_title
    MerchantDetailsTab.TERMINALS -> R.string.merchant_details_no_terminals_title
}

@StringRes
private fun MerchantDetailsTab.emptyBody(): Int = when (this) {
    MerchantDetailsTab.OUTLETS -> R.string.merchant_details_no_outlets_body
    MerchantDetailsTab.TERMINALS -> R.string.merchant_details_no_terminals_body
}

private fun formatCount(value: Long): String = CroatianFormat.count(value.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
