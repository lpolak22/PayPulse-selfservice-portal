package com.example.paypulse.ui.terminals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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

private const val SKELETON_ROWS = 5

@Composable
fun TerminalsSkeleton(modifier: Modifier = Modifier) {
    val alphaState = rememberSkeletonAlpha()
    val alpha = { alphaState.value }
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(SKELETON_ROWS) { TerminalRowSkeleton(alpha) }
    }
}

@Composable
private fun TerminalRowSkeleton(alpha: () -> Float) {
    val shape = MaterialTheme.shapes.small
    OutlinedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBlock(alpha, Modifier.size(40.dp), CircleShape)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(alpha, Modifier.width(150.dp).height(18.dp), shape)
                SkeletonBlock(alpha, Modifier.width(190.dp).height(14.dp), shape)
            }
            SkeletonBlock(alpha, Modifier.width(72.dp).height(22.dp), shape)
        }
    }
}
