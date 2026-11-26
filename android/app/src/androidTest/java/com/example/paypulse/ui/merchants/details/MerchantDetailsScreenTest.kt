package com.example.paypulse.ui.merchants.details

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.ui.theme.PayPulseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MerchantDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val events = mutableListOf<MerchantDetailsEvent>()

    private fun show(state: MerchantDetailsUiState) {
        composeRule.setContent { PayPulseTheme { MerchantDetailsScreen(state = state, onEvent = { events += it }) } }
    }

    private fun loaded(details: MerchantDetails, tab: MerchantDetailsTab = MerchantDetailsTab.OUTLETS) =
        MerchantDetailsUiState(content = MerchantDetailsContent.Loaded(details), selectedTab = tab)

    private fun text(@StringRes id: Int, vararg args: Any): String = resources.getString(id, *args)

    private fun assertShown(text: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        composeRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun tabs_showTheTotalCounts() {
        show(loaded(kvadratDetails(outletTotal = 4, terminalTotal = 9)))

        composeRule.onNodeWithText(text(R.string.merchant_details_outlets_tab, "4")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.merchant_details_terminals_tab, "9")).performClick()

        assertEquals(listOf(MerchantDetailsEvent.TabSelected(MerchantDetailsTab.TERMINALS)), events)
    }

    @Test
    fun noOutlets_showsTheOutletsEmptyState() {
        show(loaded(kvadratDetails(outlets = emptyList())))

        assertShown(text(R.string.merchant_details_no_outlets_title))
        composeRule.onNodeWithText(text(R.string.merchant_details_outlets_search)).assertDoesNotExist()
    }

    @Test
    fun noTerminals_showsTheTerminalsEmptyState() {
        show(loaded(kvadratDetails(terminals = emptyList()), MerchantDetailsTab.TERMINALS))

        assertShown(text(R.string.merchant_details_no_terminals_title))
    }

    @Test
    fun searchWithoutMatches_showsNoResults() {
        show(loaded(kvadratDetails()).copy(outletSearch = "Split"))

        assertShown(text(R.string.merchant_details_no_results_title))
    }

    @Test
    fun moreItemsThanLoaded_showsHowManyAreShown() {
        show(loaded(kvadratDetails(outletTotal = 120)))

        assertShown(text(R.string.merchant_details_shown_count, "2", "120"))
    }

    @Test
    fun networkError_offersRetry() {
        show(MerchantDetailsUiState(content = MerchantDetailsContent.Error(MerchantDetailsError.NETWORK)))

        composeRule.onNodeWithText(text(R.string.merchants_error_network_title), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.merchants_retry)).performClick()

        assertEquals(listOf(MerchantDetailsEvent.Retry), events)
    }
}
