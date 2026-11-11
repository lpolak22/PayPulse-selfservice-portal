package com.example.paypulse.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.SkeletonBlock
import com.example.paypulse.ui.components.rememberSkeletonAlpha

private const val SKELETON_ROWS = 8

@Composable
fun TransactionsSkeleton(modifier: Modifier = Modifier) {
    val alphaState = rememberSkeletonAlpha()
    val alpha = { alphaState.value }
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBlock(alpha, Modifier.width(120.dp).height(14.dp), MaterialTheme.shapes.small)
        repeat(SKELETON_ROWS) { TransactionRowSkeleton(alpha) }
    }
}

@Composable
private fun TransactionRowSkeleton(alpha: () -> Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBlock(alpha, Modifier.size(40.dp), MaterialTheme.shapes.small)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBlock(alpha, Modifier.width(140.dp).height(14.dp), MaterialTheme.shapes.small)
            SkeletonBlock(alpha, Modifier.width(180.dp).height(12.dp), MaterialTheme.shapes.small)
        }
        SkeletonBlock(alpha, Modifier.width(72.dp).height(14.dp), MaterialTheme.shapes.small)
    }
}
