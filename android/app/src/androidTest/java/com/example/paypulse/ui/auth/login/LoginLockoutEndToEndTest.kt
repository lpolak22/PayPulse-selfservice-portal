package com.example.paypulse.ui.auth.login

import android.Manifest
import android.os.Build
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paypulse.R
import com.example.paypulse.ui.theme.PayPulseTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

private const val MAX_ATTEMPTS = 5
private const val LOCKOUT_MINUTES = 15
private const val WRONG_PASSWORD = "Pogresna-Lozinka-1"
private const val SERVER_ANSWER_TIMEOUT_MILLIS = 15_000L

@RunWith(AndroidJUnit4::class)
class LoginLockoutEndToEndTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val resources = instrumentation.targetContext.resources

    @Before
    fun allowReachingTheLocalBackend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )
        }
    }

    @Test
    fun theLockoutMessageAppearsAfterTheLastFailedAttempt() {
        composeRule.setContent { PayPulseTheme { LoginRoute(onForgotPasswordClick = {}) } }
        composeRule.onNodeWithText(text(R.string.login_email_label)).performTextInput(neverRegisteredEmail())

        for (remaining in MAX_ATTEMPTS - 1 downTo 1) {
            submitWrongPassword()
            awaitMessage(plural(R.plurals.login_error_invalid_credentials_remaining, remaining))
        }
        submitWrongPassword()

        awaitMessage(plural(R.plurals.login_error_account_locked_minutes, LOCKOUT_MINUTES))
    }

    private fun submitWrongPassword() {
        composeRule.onNodeWithText(text(R.string.login_password_label)).performTextInput(WRONG_PASSWORD)
        composeRule.onNodeWithText(text(R.string.login_button)).performClick()
    }

    private fun awaitMessage(message: String) {
        composeRule.waitUntil(SERVER_ANSWER_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    private fun neverRegisteredEmail(): String = "e2e-lockout-${UUID.randomUUID()}@paypulse.test"

    private fun text(@StringRes id: Int): String = resources.getString(id)

    private fun plural(@PluralsRes id: Int, count: Int): String = resources.getQuantityString(id, count, count)
}
