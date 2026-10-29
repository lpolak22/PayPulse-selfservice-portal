package com.example.paypulse.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import com.example.paypulse.ui.theme.PayPulseTheme
import java.time.LocalDateTime

private val sampleAdmin = User(
    id = 1,
    firstName = "Ana",
    lastName = "Horvat",
    email = "ana.horvat@primjer.hr",
    phone = null,
    role = UserRole.ADMIN,
    status = UserStatus.ACTIVE,
    createdAt = LocalDateTime.of(2026, 10, 1, 9, 0)
)

@PreviewLightDark
@Composable
private fun HomeAdminPreview() = HomePreview(user = sampleAdmin)

@PreviewLightDark
@Composable
private fun HomeUserPreview() = HomePreview(
    user = sampleAdmin.copy(id = 2, firstName = "Ivan", lastName = "Kovač", email = "ivan.kovac@primjer.hr", role = UserRole.USER)
)

@Composable
private fun HomePreview(user: User) {
    PayPulseTheme {
        HomeScreen(user = user, onLogoutClick = {})
    }
}
