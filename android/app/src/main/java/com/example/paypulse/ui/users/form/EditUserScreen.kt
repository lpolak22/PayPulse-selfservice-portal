package com.example.paypulse.ui.users.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserField
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus

@Composable
fun EditUserScreen(
    values: UserFormValues,
    state: EditUserUiState,
    onFieldChange: (UserField, String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { UserFormTopBar(title = R.string.users_edit_title, onBackClick = onBackClick) },
        bottomBar = { if (state.user != null) EditUserBottomBar(state.form, onSaveClick, onDeleteClick) }
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.isLoading -> CenteredLoading(contentModifier)
            state.user == null -> NotFoundMessage(contentModifier)
            else -> EditUserContent(state.user, values, state, onFieldChange, onRoleChange, onActiveChange, contentModifier)
        }
    }
    EditUserDialogs(state = state, onConfirm = onDialogConfirm, onDismiss = onDialogDismiss)
}

@Composable
private fun EditUserContent(
    user: User,
    values: UserFormValues,
    state: EditUserUiState,
    onFieldChange: (UserField, String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserHeader(user = user)
        UserFormFields(values = values, state = state.form, onFieldChange = onFieldChange)
        RoleOptionCards(selected = state.form.role, onSelect = onRoleChange)
        ActiveSwitchRow(
            isActive = state.status == UserStatus.ACTIVE,
            enabled = state.canToggleActive,
            onActiveChange = onActiveChange
        )
    }
}

@Composable
private fun EditUserBottomBar(form: UserFormUiState, onSaveClick: () -> Unit, onDeleteClick: () -> Unit) {
    FormBottomBar {
        form.formError?.let { FormErrorText(it) }
        SaveButton(text = R.string.users_save_button, isSaving = form.isSaving, onClick = onSaveClick)
        TextButton(
            onClick = onDeleteClick,
            enabled = !form.isSaving,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.users_delete_button))
        }
    }
}

@Composable
private fun CenteredLoading(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotFoundMessage(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.users_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}