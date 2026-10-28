package com.example.paypulse.ui.users.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.User
import com.example.paypulse.ui.components.SearchField

@Composable
fun UsersListScreen(
    state: UsersListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onAddUser: () -> Unit,
    onUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = { AddUserButton(onClick = onAddUser) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            UsersHeader(totalCount = state.totalCount)
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.users_search_hint),
                modifier = Modifier.fillMaxWidth()
            )
            UsersContent(state = state, isSearching = query.isNotBlank(), onUserClick = onUserClick)
        }
    }
}

@Composable
private fun UsersHeader(totalCount: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.users_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f).alignByBaseline()
        )
        Text(
            text = pluralStringResource(R.plurals.users_count, totalCount, totalCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alignByBaseline()
        )
    }
}

@Composable
private fun UsersContent(state: UsersListUiState, isSearching: Boolean, onUserClick: (Long) -> Unit) {
    when {
        state.isLoading -> LoadingIndicator()
        state.users.isEmpty() && isSearching -> EmptyMessage(stringResource(R.string.users_search_empty))
        state.users.isEmpty() -> EmptyMessage(stringResource(R.string.users_empty))
        else -> UsersList(users = state.users, onUserClick = onUserClick)
    }
}

@Composable
private fun UsersList(users: List<User>, onUserClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserRow(user = user, onClick = { onUserClick(user.id) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddUserButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
        text = { Text(stringResource(R.string.users_add_button)) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}