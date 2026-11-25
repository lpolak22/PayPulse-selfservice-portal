package com.example.paypulse.ui.accessibility

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paypulse.R
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.ui.theme.PayPulseTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilitySettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    private var state by mutableStateOf(AccessibilitySettings())

    @Before
    fun showScreen() {
        composeRule.setContent {
            PayPulseTheme {
                AccessibilitySettingsScreen(
                    state = state,
                    onLargerTextChange = { state = state.copy(largerText = it) },
                    onHighContrastChange = { state = state.copy(highContrast = it) },
                    onMenuClick = {}
                )
            }
        }
    }

    @Test
    fun theHeadingIsShown() {
        composeRule.onNode(hasText(text(R.string.accessibility_title)) and isHeading)
            .assertIsDisplayed()
    }

    @Test
    fun clickingLargerTextTogglesIt() {
        assertRowToggles(R.string.accessibility_larger_text) { state.largerText }
    }

    @Test
    fun clickingHighContrastTogglesIt() {
        assertRowToggles(R.string.accessibility_high_contrast) { state.highContrast }
    }

    @Test
    fun eachRowIsOneSwitchNode() {
        composeRule.onAllNodes(isToggleable()).assertCountEquals(2)
        listOf(R.string.accessibility_larger_text, R.string.accessibility_high_contrast).forEach {
            composeRule.onAllNodesWithText(text(it)).assertCountEquals(1)
            row(it).assert(isToggleable()).assert(isSwitch)
        }
    }

    private fun assertRowToggles(@StringRes label: Int, enabled: () -> Boolean) {
        row(label).assertIsOff()
        row(label).performClick()
        row(label).assertIsOn()
        assertTrue(enabled())
        row(label).performClick()
        row(label).assertIsOff()
        assertFalse(enabled())
    }

    private fun row(@StringRes label: Int): SemanticsNodeInteraction =
        composeRule.onNode(hasText(text(label)) and isToggleable())

    private fun text(@StringRes id: Int) = resources.getString(id)

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    private val isSwitch = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
}
