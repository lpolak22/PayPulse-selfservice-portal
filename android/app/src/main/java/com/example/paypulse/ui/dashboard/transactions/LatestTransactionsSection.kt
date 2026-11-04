package com.example.paypulse.ui.dashboard.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.LatestTransaction
import java.time.LocalDate

@Composable
fun LatestTransactionsSection(
    transactions: List<LatestTransaction>,
    today: LocalDate,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) return
    val rows = remember(transactions, today) { transactions.map { it.toRowModel(today) } }
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(onShowAll = onShowAll)
        rows.forEach { row -> LatestTransactionRow(row = row) }
    }
}

@Composable
private fun SectionHeader(onShowAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.dashboard_latest_transactions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        TextButton(onClick = onShowAll, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(
                text = stringResource(R.string.dashboard_show_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
