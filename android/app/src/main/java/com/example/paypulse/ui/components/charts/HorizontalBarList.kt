package com.example.paypulse.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalBarList(
    items: List<HorizontalBarItem>,
    selectedIndex: Int?,
    onItemClick: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val maxValue = items.maxOfOrNull { it.value } ?: 0f
    Column(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .selectableGroup()
    ) {
        items.forEachIndexed { index, item ->
            val fraction = if (maxValue > 0f) item.value / maxValue else 0f
            BarRow(item, fraction, isSelected = index == selectedIndex, onClick = { onItemClick(index) })
        }
    }
}

@Composable
private fun BarRow(item: HorizontalBarItem, fraction: Float, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val textColor = if (isSelected) colors.onPrimaryContainer else colors.onSurface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .then(if (isSelected) Modifier.background(colors.primaryContainer) else Modifier)
            .selectable(selected = isSelected, onClick = onClick, role = Role.Button)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = item.label, style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.weight(1f))
            Text(text = item.valueLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
        BarTrack(fraction = fraction)
    }
}

@Composable
private fun BarTrack(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
