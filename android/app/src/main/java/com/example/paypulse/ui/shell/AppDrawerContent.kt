package com.example.paypulse.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.User

@Composable
fun AppDrawerContent(
    user: User,
    selectedItem: DrawerItem?,
    onItemClick: (DrawerItem) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        DrawerHeader(user = user)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            OptionsLabel()
            DrawerItems(DrawerItem.visibleFor(user.role, DrawerGroup.MAIN), selectedItem, onItemClick)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            DrawerItems(DrawerItem.visibleFor(user.role, DrawerGroup.SECONDARY), selectedItem, onItemClick)
        }
        LogoutItem(onClick = onLogoutClick, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun OptionsLabel() {
    Text(
        text = stringResource(R.string.navigation_section_options),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { heading() }
    )
}

@Composable
private fun DrawerItems(items: List<DrawerItem>, selectedItem: DrawerItem?, onItemClick: (DrawerItem) -> Unit) {
    items.forEach { item ->
        DrawerEntry(item = item, selected = item == selectedItem, onClick = { onItemClick(item) })
    }
}

@Composable
private fun LogoutItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val red = MaterialTheme.colorScheme.primary
    NavigationDrawerItem(
        label = { Text(stringResource(R.string.navigation_logout)) },
        icon = { Icon(painterResource(R.drawable.ic_logout), contentDescription = null) },
        selected = false,
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = red, unselectedTextColor = red)
    )
}

@Composable
private fun DrawerEntry(item: DrawerItem, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationDrawerItem(
        label = { Text(stringResource(item.label)) },
        icon = { Icon(painterResource(item.icon), contentDescription = null) },
        selected = selected,
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.primaryContainer,
            selectedIconColor = colors.onPrimaryContainer,
            selectedTextColor = colors.onPrimaryContainer,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurface
        )
    )
}
