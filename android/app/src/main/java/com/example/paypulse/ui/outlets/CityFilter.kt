package com.example.paypulse.ui.outlets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R

@Composable
fun CityFilter(
    selected: String?,
    cities: List<String>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            enabled = cities.isNotEmpty() || selected != null,
            label = { Text(text = selected ?: stringResource(R.string.outlets_city_filter)) },
            trailingIcon = { Icon(painterResource(R.drawable.ic_filter_list), contentDescription = null) },
            modifier = Modifier.heightIn(min = 48.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CityOption(text = stringResource(R.string.outlets_city_all)) { expanded = false; onSelect(null) }
            cities.forEach { city ->
                CityOption(text = city) { expanded = false; onSelect(city) }
            }
        }
    }
}

@Composable
private fun CityOption(text: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(text = text) }, onClick = onClick, modifier = Modifier.heightIn(min = 48.dp))
}
