package com.example.paypulse.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChartLegend(
    items: List<LegendItem>,
    selectedIndex: Int?,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup()) {
        items.forEachIndexed { index, item ->
            LegendRow(item = item, isSelected = index == selectedIndex, onClick = { onItemClick(index) })
        }
    }
}

@Composable
private fun LegendRow(item: LegendItem, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val textColor = if (isSelected) colors.onPrimaryContainer else colors.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .then(if (isSelected) Modifier.background(colors.primaryContainer) else Modifier)
            .selectable(selected = isSelected, onClick = onClick, role = Role.Button)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendSwatch(color = item.color, outlined = item.outlined)
        Text(text = item.label, style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.weight(1f))
        Text(text = item.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun LegendSwatch(color: Color, outlined: Boolean) {
    val border = if (outlined) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall) else Modifier
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color)
            .then(border)
    )
}
