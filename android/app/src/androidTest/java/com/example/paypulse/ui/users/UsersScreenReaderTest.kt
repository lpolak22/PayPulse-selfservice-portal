package com.example.paypulse.ui.users

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import com.example.paypulse.ui.theme.PayPulseTheme
import com.example.paypulse.ui.users.form.NewUserScreen
import com.example.paypulse.ui.users.form.UserFormUiState
import com.example.paypulse.ui.users.form.UserFormValues
import com.example.paypulse.ui.users.list.UsersListScreen
import com.example.paypulse.ui.users.list.UsersListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

private val sampleUser =
    User(1, "Ana", "Horvat", "ana.horvat@trgovac.hr", null, UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 0))
private val filledValues = UserFormValues("Petra", "Novak", "petra.novak@primjer.hr", "+385 91 234 5678")

private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

private fun hasClickLabel(label: String) = SemanticsMatcher("OnClick label is \"$label\"") { node ->
    node.config.getOrNull(SemanticsActions.OnClick)?.label == label
}

@RunWith(AndroidJUnit4::class)
class UsersScreenReaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun XP3_users_title_is_heading() {
        showUsersList(UsersListUiState(isLoading = false, users = listOf(sampleUser), totalCount = 1))

        composeRule.onNodeWithText("Korisnici").assert(isHeading)
    }

    @Test
    fun XP3_role_label_is_heading() {
        showNewUser(UserFormUiState())

        composeRule.onNodeWithText("Uloga").assert(isHeading)
    }

    @Test
    fun XP3_form_top_bar_title_is_heading() {
        showNewUser(UserFormUiState())

        composeRule.onNodeWithText("Novi korisnik").assert(isHeading)
    }

    @Test
    fun XP3_user_row_has_click_label_uredi_korisnika() {
        showUsersList(UsersListUiState(isLoading = false, users = listOf(sampleUser), totalCount = 1))

        composeRule.onNode(hasText("Ana Horvat") and hasClickAction()).assert(hasClickLabel("Uredi korisnika"))
    }

    @Test
    fun XP3_save_button_while_saving_has_description() {
        showNewUser(UserFormUiState(isSaving = true))

        composeRule.onNodeWithContentDescription("Spremanje u tijeku").assertIsDisplayed()
    }

    @Test
    fun XP3_loading_list_has_description() {
        showUsersList(UsersListUiState())

        composeRule.onNodeWithContentDescription("Učitavanje").assertIsDisplayed()
    }

    private fun showUsersList(state: UsersListUiState) {
        composeRule.setContent {
            PayPulseTheme {
                UsersListScreen(
                    state = state,
                    query = "",
                    onQueryChange = {},
                    onAddUser = {},
                    onUserClick = {},
                    onMenuClick = {}
                )
            }
        }
    }

    private fun showNewUser(state: UserFormUiState) {
        composeRule.setContent {
            PayPulseTheme {
                NewUserScreen(
                    values = filledValues,
                    state = state,
                    onFieldChange = { _, _ -> },
                    onRoleChange = {},
                    onSaveClick = {},
                    onBackClick = {}
                )
            }
        }
    }
}
