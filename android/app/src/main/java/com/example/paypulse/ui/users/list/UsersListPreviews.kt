package com.example.paypulse.ui.users.list

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import com.example.paypulse.ui.theme.PayPulseTheme
import java.time.LocalDateTime

private val previewUsers = listOf(
    User(1, "Ana", "Horvat", "ana.horvat@trgovac.hr", null, UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 0)),
    User(2, "Ivan", "Babić", "ivan.babic@trgovac.hr", null, UserRole.USER, UserStatus.PENDING_ACTIVATION, LocalDateTime.of(2026, 9, 3, 10, 30)),
    User(3, "Lucija", "Jurić", "lucija.juric@trgovac.hr", null, UserRole.USER, UserStatus.INACTIVE, LocalDateTime.of(2026, 9, 20, 11, 0))
)

@Composable
private fun PreviewScreen(state: UsersListUiState, query: String = "") {
    PayPulseTheme {
        UsersListScreen(
            state = state,
            query = query,
            onQueryChange = {},
            onAddUser = {},
            onUserClick = {}
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UsersListPreview() {
    PreviewScreen(UsersListUiState(isLoading = false, users = previewUsers, totalCount = previewUsers.size))
}

@Preview(name = "No search results", showBackground = true)
@Composable
private fun UsersListNoResultsPreview() {
    PreviewScreen(UsersListUiState(isLoading = false, users = emptyList(), totalCount = 5), query = "xyz")
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun UsersListLoadingPreview() {
    PreviewScreen(UsersListUiState())
}