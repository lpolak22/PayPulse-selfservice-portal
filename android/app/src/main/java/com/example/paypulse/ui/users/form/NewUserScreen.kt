package com.example.paypulse.ui.users.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserField
import com.example.paypulse.domain.user.UserRole

@Composable
fun NewUserScreen(
    values: UserFormValues,
    state: UserFormUiState,
    onFieldChange: (UserField, String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { UserFormTopBar(title = R.string.users_new_title, onBackClick = onBackClick) },
        bottomBar = { CreateUserBottomBar(state = state, onSaveClick = onSaveClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserFormFields(values = values, state = state, onFieldChange = onFieldChange)
            RoleOptionCards(selected = state.role, onSelect = onRoleChange)
        }
    }
}

@Composable
private fun CreateUserBottomBar(state: UserFormUiState, onSaveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.formError?.let { FormErrorText(it) }
        Text(
            text = stringResource(R.string.users_activation_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        SaveButton(text = R.string.users_create_button, isSaving = state.isSaving, onClick = onSaveClick)
    }
}