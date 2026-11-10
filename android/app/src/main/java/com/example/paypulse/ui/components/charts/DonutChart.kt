package com.example.paypulse.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    centerValue: String,
    selectedIndex: Int?,
    onSliceClick: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 140.dp
) {
    val style = DonutStyle(ringWidth = 18.dp, selectedRingWidth = 26.dp, borderColor = MaterialTheme.colorScheme.outline)
    val currentOnSliceClick by rememberUpdatedState(onSliceClick)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(slices) {
                    detectTapGestures { offset ->
                        donutSliceAt(offset, size.width.toFloat(), slices, style.selectedRingWidth.toPx())
                            ?.let(currentOnSliceClick)
                    }
                },
            contentDescription = contentDescription
        ) {
            drawDonut(slices, selectedIndex, style)
        }
        DonutCenter(label = centerLabel, value = centerValue)
    }
}

@Composable
private fun DonutCenter(label: String, value: String) {
    Column(
        modifier = Modifier.clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
