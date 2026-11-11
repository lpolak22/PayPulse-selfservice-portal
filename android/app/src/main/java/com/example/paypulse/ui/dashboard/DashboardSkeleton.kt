package com.example.paypulse.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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

private const val TILE_ROWS = 2
private const val TRANSACTION_ROWS = 3

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    val alphaState = rememberSkeletonAlpha()
    val alpha = { alphaState.value }
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonBlock(alpha, Modifier.fillMaxWidth().height(132.dp), MaterialTheme.shapes.large)
        repeat(TILE_ROWS) { TileRowSkeleton(alpha) }
        TitleSkeleton(alpha, Modifier.padding(top = 24.dp))
        SkeletonBlock(alpha, Modifier.fillMaxWidth().height(180.dp))
        TitleSkeleton(alpha, Modifier.padding(top = 24.dp))
        BrandSkeleton(alpha)
        TitleSkeleton(alpha, Modifier.padding(top = 24.dp))
        repeat(TRANSACTION_ROWS) { TransactionRowSkeleton(alpha) }
    }
}

@Composable
private fun TileRowSkeleton(alpha: () -> Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonBlock(alpha, Modifier.weight(1f).height(76.dp))
        SkeletonBlock(alpha, Modifier.weight(1f).height(76.dp))
    }
}

@Composable
private fun TitleSkeleton(alpha: () -> Float, modifier: Modifier = Modifier) {
    SkeletonBlock(alpha, modifier.width(180.dp).height(20.dp), MaterialTheme.shapes.small)
}

@Composable
private fun BrandSkeleton(alpha: () -> Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBlock(alpha, Modifier.size(140.dp), CircleShape)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { SkeletonBlock(alpha, Modifier.fillMaxWidth().height(16.dp), MaterialTheme.shapes.small) }
        }
    }
}

@Composable
private fun TransactionRowSkeleton(alpha: () -> Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBlock(alpha, Modifier.size(40.dp), MaterialTheme.shapes.small)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBlock(alpha, Modifier.width(140.dp).height(14.dp), MaterialTheme.shapes.small)
            SkeletonBlock(alpha, Modifier.width(100.dp).height(12.dp), MaterialTheme.shapes.small)
        }
        SkeletonBlock(alpha, Modifier.width(64.dp).height(14.dp), MaterialTheme.shapes.small)
    }
}
