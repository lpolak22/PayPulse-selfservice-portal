package com.example.paypulse.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.paypulse.R

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) { { Text(errorMessage) } } else null,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        trailingIcon = { PasswordVisibilityButton(isPasswordVisible, onVisibilityToggle) }
    )
}

@Composable
private fun PasswordVisibilityButton(isPasswordVisible: Boolean, onClick: () -> Unit) {
    val icon = if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
    val description = if (isPasswordVisible) R.string.password_hide else R.string.password_show
    IconButton(onClick = onClick) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(description))
    }
}
