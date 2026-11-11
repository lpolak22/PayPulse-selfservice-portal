package com.example.paypulse.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
fun TransactionRow(row: TransactionRowModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brand = stringResource(row.brand.label())
    val card = row.lastDigits?.let { stringResource(R.string.transactions_card, brand, it) } ?: brand
    val status = stringResource(row.status?.label() ?: R.string.transactions_status_unknown)
    val terminal = row.terminalId?.let { stringResource(R.string.transactions_terminal, it) }
    val channel = row.channel?.let { stringResource(it.label()) }
    val subtitle = listOfNotNull(row.time, row.terminalId, channel)
        .joinToString(stringResource(R.string.transactions_row_separator))
    val description = listOfNotNull(card, row.dateTime, terminal, channel, row.amount, status)
        .joinToString(stringResource(R.string.transactions_description_separator))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClickLabel = stringResource(R.string.transactions_open_details), onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardIcon()
        TransactionTitleAndSubtitle(title = card, subtitle = subtitle, modifier = Modifier.weight(1f))
        AmountAndStatus(
            amount = row.amount,
            status = status,
            statusColor = row.status?.color() ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
