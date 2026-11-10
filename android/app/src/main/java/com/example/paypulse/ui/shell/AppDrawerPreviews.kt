package com.example.paypulse.ui.shell

import androidx.compose.material3.ModalDrawerSheet
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
    createdAt = LocalDateTime.of(2026, 10, 1, 9, 0),
    organisationName = "Trgovina Kvadrat d.o.o."
)

private val sampleUser = sampleAdmin.copy(id = 2, firstName = "Ivan", lastName = "Kovač", role = UserRole.USER)

@PreviewLightDark
@Composable
private fun AppDrawerAdminPreview() = DrawerPreview(user = sampleAdmin, selectedItem = DrawerItem.HOME)

@PreviewLightDark
@Composable
private fun AppDrawerUserPreview() = DrawerPreview(user = sampleUser, selectedItem = DrawerItem.TRANSACTIONS)

@PreviewLightDark
@Composable
private fun ComingSoonPreview() {
    PayPulseTheme {
        ComingSoonScreen(title = "Terminali", onMenuClick = {})
    }
}

@Composable
private fun DrawerPreview(user: User, selectedItem: DrawerItem) {
    PayPulseTheme {
        ModalDrawerSheet {
            AppDrawerContent(user = user, selectedItem = selectedItem, onItemClick = {}, onLogoutClick = {})
        }
    }
}
