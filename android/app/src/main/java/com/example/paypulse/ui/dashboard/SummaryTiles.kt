package com.example.paypulse.ui.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DashboardSummary
import com.example.paypulse.ui.components.CroatianFormat

@Composable
fun SummaryTiles(summary: DashboardSummary, modifier: Modifier = Modifier) {
    val missing = stringResource(R.string.dashboard_value_missing)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TileRow {
            SummaryTile(R.string.dashboard_tile_transactions, CroatianFormat.count(summary.totalCount))
            SummaryTile(R.string.dashboard_tile_average_amount, summary.averageAmount?.let(CroatianFormat::money) ?: missing)
        }
        TileRow {
            SummaryTile(R.string.dashboard_tile_success_rate, summary.successRate?.let(CroatianFormat::percent) ?: missing)
            SummaryTile(R.string.dashboard_tile_declined, CroatianFormat.count(summary.declinedCount))
        }
    }
}

@Composable
private fun TileRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun RowScope.SummaryTile(@StringRes label: Int, value: String) {
    Surface(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp).semantics(mergeDescendants = true) {}) {
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
