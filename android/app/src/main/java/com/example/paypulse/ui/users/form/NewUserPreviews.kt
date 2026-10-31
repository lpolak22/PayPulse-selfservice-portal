package com.example.paypulse.ui.users.form

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.ui.theme.PayPulseTheme

private val emptyValues = UserFormValues("", "", "", "")
private val filledValues = UserFormValues("Petra", "Novak", "petra.novak@primjer.hr", "+385 91 234 5678")

@Composable
private fun PreviewScreen(values: UserFormValues, state: UserFormUiState) {
    PayPulseTheme {
        NewUserScreen(
            values = values,
            state = state,
            onFieldChange = { _, _ -> },
            onRoleChange = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Empty, light", showBackground = true)
@Preview(name = "Empty, dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NewUserEmptyPreview() {
    PreviewScreen(emptyValues, UserFormUiState())
}

@Preview(name = "Filled, Admin", showBackground = true)
@Composable
private fun NewUserFilledPreview() {
    PreviewScreen(filledValues, UserFormUiState(role = UserRole.ADMIN))
}

@Preview(name = "Errors", showBackground = true)
@Composable
private fun NewUserErrorsPreview() {
    PreviewScreen(
        filledValues.copy(firstName = "", phone = "091"),
        UserFormUiState(
            firstNameError = R.string.users_error_required,
            emailError = R.string.users_error_email_exists,
            phoneError = R.string.users_error_phone_invalid
        )
    )
}

@Preview(name = "Saving", showBackground = true)
@Composable
private fun NewUserSavingPreview() {
    PreviewScreen(filledValues, UserFormUiState(isSaving = true))
}