package com.example.paypulse.ui.merchants.details

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paypulse.R
import com.example.paypulse.ui.theme.PayPulseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val OWN_MERCHANT_ID = 7L
private const val OTHER_ORGANISATION_MERCHANT_ID = 4L

@RunWith(AndroidJUnit4::class)
class MerchantDetailsRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val navigationCalls = mutableListOf<String>()
    private val navigation = MerchantDetailsNavigation(
        onBackClick = { navigationCalls += "back" },
        onMerchantsClick = { navigationCalls += "merchants" },
        onOutletClick = { navigationCalls += "outlet $it" },
        onTerminalClick = { navigationCalls += "terminal $it" },
        onAllOutletsClick = { id, name -> navigationCalls += "all outlets $id $name" }
    )

    private fun openMerchant(id: Long) {
        val viewModel = MerchantDetailsViewModel(
            OrganisationMerchantRepository(mapOf(OWN_MERCHANT_ID to kvadratDetails())),
            SavedStateHandle(mapOf(MERCHANT_ID_KEY to id))
        )
        composeRule.setContent { PayPulseTheme { MerchantDetailsRoute(navigation = navigation, viewModel = viewModel) } }
        composeRule.waitForIdle()
    }

    private fun text(@StringRes id: Int, vararg args: Any): String = resources.getString(id, *args)

    @Test
    fun ownMerchant_showsProfileOutletsAndTerminals() {
        openMerchant(OWN_MERCHANT_ID)

        composeRule.onAllNodesWithText("Trgovina Kvadrat d.o.o.", useUnmergedTree = true).assertCountEquals(2)
        composeRule.onNodeWithText(text(R.string.merchants_title)).assertIsDisplayed()
        composeRule.onNodeWithText("TR-100245", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("12345678901", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.merchants_status_active), useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Ilica 12, 10000 Zagreb", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("PM-0001 · 3 terminala", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText(text(R.string.merchant_details_terminals_tab, "1")).performClick()

        composeRule.onNodeWithText("TK000101", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun outletAndTerminalRows_linkToTheirDetails() {
        openMerchant(OWN_MERCHANT_ID)

        composeRule.onNodeWithText("PM Velika Gorica", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText(text(R.string.merchant_details_terminals_tab, "1")).performClick()
        composeRule.onNodeWithText("TK000101", useUnmergedTree = true).performClick()

        assertEquals(listOf("outlet 14", "terminal 21"), navigationCalls)
    }

    @Test
    fun showAllOutlets_opensTheFullOutletListOfThisMerchant() {
        openMerchant(OWN_MERCHANT_ID)
        val showAll = text(R.string.merchant_details_all_outlets)

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(showAll))
        composeRule.onNodeWithText(showAll).performClick()

        assertEquals(listOf("all outlets $OWN_MERCHANT_ID Trgovina Kvadrat d.o.o."), navigationCalls)
    }

    @Test
    fun breadcrumbAndBackArrow_leadBackToMerchants() {
        openMerchant(OWN_MERCHANT_ID)

        composeRule.onNodeWithText(text(R.string.merchants_title)).performClick()
        composeRule.onNodeWithContentDescription(text(R.string.merchant_details_back)).performClick()

        assertEquals(listOf("merchants", "back"), navigationCalls)
    }

    @Test
    fun searching_showsOnlyMatchingOutlets() {
        openMerchant(OWN_MERCHANT_ID)

        composeRule.onNodeWithText(text(R.string.merchant_details_outlets_search)).performTextInput("Gorica")

        composeRule.onNodeWithText("PM Velika Gorica", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("PM Ilica 12", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun merchantOfAnotherOrganisation_showsTheNotFoundPage() {
        openMerchant(OTHER_ORGANISATION_MERCHANT_ID)

        composeRule.onNodeWithText(text(R.string.merchant_details_not_found_title), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Trgovina Kvadrat d.o.o.", useUnmergedTree = true).assertDoesNotExist()

        composeRule.onNodeWithText(text(R.string.merchant_details_back_to_merchants)).performClick()

        assertEquals(listOf("merchants"), navigationCalls)
    }
}
