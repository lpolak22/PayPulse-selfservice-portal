package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserField

data class UserFormValues(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)

@Composable
internal fun UserFormFields(
    values: UserFormValues,
    state: UserFormUiState,
    onFieldChange: (UserField, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NameFields(values = values, state = state, onFieldChange = onFieldChange)
        FormTextField(
            value = values.email,
            onValueChange = { onFieldChange(UserField.EMAIL, it) },
            label = R.string.users_email_label,
            error = state.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        FormTextField(
            value = values.phone,
            onValueChange = { onFieldChange(UserField.PHONE, it) },
            label = R.string.users_phone_label,
            error = state.phoneError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NameFields(values: UserFormValues, state: UserFormUiState, onFieldChange: (UserField, String) -> Unit) {
    val nameKeyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FormTextField(
            value = values.firstName,
            onValueChange = { onFieldChange(UserField.FIRST_NAME, it) },
            label = R.string.users_first_name_label,
            error = state.firstNameError,
            keyboardOptions = nameKeyboard,
            modifier = Modifier.weight(1f)
        )
        FormTextField(
            value = values.lastName,
            onValueChange = { onFieldChange(UserField.LAST_NAME, it) },
            label = R.string.users_last_name_label,
            error = state.lastNameError,
            keyboardOptions = nameKeyboard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    @StringRes error: Int?,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(stringResource(error)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}