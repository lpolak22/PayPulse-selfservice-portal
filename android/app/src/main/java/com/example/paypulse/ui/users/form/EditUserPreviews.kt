package com.example.paypulse.ui.users.form

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import com.example.paypulse.ui.theme.PayPulseTheme
import java.time.LocalDateTime

private val previewUser = User(
    2, "Ivan", "Babić", "ivan.babic@primjer.hr", "+385987654321",
    UserRole.USER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 3, 10, 30)
)
private val previewValues = UserFormValues("Ivan", "Babić", "ivan.babic@primjer.hr", "+385 98 765 4321")

@Composable
private fun PreviewScreen(state: EditUserUiState) {
    PayPulseTheme {
        EditUserScreen(
            values = previewValues,
            state = state,
            onFieldChange = { _, _ -> },
            onRoleChange = {},
            onActiveChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Active, light", showBackground = true)
@Preview(name = "Active, dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditUserActivePreview() {
    PreviewScreen(EditUserUiState(user = previewUser, isLoading = false))
}

@Preview(name = "Pending activation", showBackground = true)
@Composable
private fun EditUserPendingPreview() {
    PreviewScreen(
        EditUserUiState(
            user = previewUser.copy(status = UserStatus.PENDING_ACTIVATION),
            status = UserStatus.PENDING_ACTIVATION,
            isLoading = false
        )
    )
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun EditUserLoadingPreview() {
    PreviewScreen(EditUserUiState())
}