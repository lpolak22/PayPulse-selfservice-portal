package com.example.paypulse.ui.dashboard.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.transaction.AmountAndStatus
import com.example.paypulse.ui.components.transaction.CardIcon
import com.example.paypulse.ui.components.transaction.TransactionTitleAndSubtitle
import com.example.paypulse.ui.dashboard.color
import com.example.paypulse.ui.dashboard.label

@Composable
fun LatestTransactionRow(row: LatestTransactionRowModel, modifier: Modifier = Modifier) {
    val brand = stringResource(row.brand.label())
    val status = stringResource(row.status.label())
    val description = rowDescription(row, brand, status)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clearAndSetSemantics { contentDescription = description }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardIcon()
        TransactionTitleAndSubtitle(
            title = row.lastDigits?.let { stringResource(R.string.dashboard_transaction_card, brand, it) } ?: brand,
            subtitle = stringResource(R.string.dashboard_transaction_place_time, row.outletName, row.time),
            modifier = Modifier.weight(1f)
        )
        AmountAndStatus(amount = row.amount, status = status, statusColor = row.status.color())
    }
}

@Composable
private fun rowDescription(row: LatestTransactionRowModel, brand: String, status: String): String {
    val lastDigits = row.lastDigits ?: return stringResource(
        R.string.dashboard_transaction_description_without_card,
        brand, row.outletName, row.time, row.amount, status
    )
    return stringResource(
        R.string.dashboard_transaction_description,
        brand, lastDigits, row.outletName, row.time, row.amount, status
    )
}
