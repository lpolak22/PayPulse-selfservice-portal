package com.example.paypulse.ui.merchants.details

import com.example.paypulse.domain.merchant.MerchantTerminal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantDetailsUiStateTest {

    private fun loaded(outletSearch: String = "", terminalSearch: String = "") = MerchantDetailsUiState(
        content = MerchantDetailsContent.Loaded(merchantDetails()),
        outletSearch = outletSearch,
        terminalSearch = terminalSearch
    )

    @Test
    fun blankSearch_showsEverything() {
        val state = loaded(outletSearch = "   ")
        assertEquals(listOf(ilica, avenueMall, velikaGorica), state.visibleOutlets)
        assertEquals(listOf(ilicaTerminal, mallTerminal), state.visibleTerminals)
    }

    @Test
    fun outletSearch_matchesNameCodeAddressCityAndPostalCode_ignoringCase() {
        assertEquals(listOf(avenueMall), loaded(outletSearch = "avenue").visibleOutlets)
        assertEquals(listOf(velikaGorica), loaded(outletSearch = "pm-0004").visibleOutlets)
        assertEquals(listOf(velikaGorica), loaded(outletSearch = "zagrebačka").visibleOutlets)
        assertEquals(listOf(velikaGorica), loaded(outletSearch = " velika ").visibleOutlets)
        assertEquals(listOf(ilica), loaded(outletSearch = "10000").visibleOutlets)
    }

    @Test
    fun terminalSearch_matchesTidAndOutletName() {
        assertEquals(listOf(mallTerminal), loaded(terminalSearch = "tk0002").visibleTerminals)
        assertEquals(listOf(ilicaTerminal), loaded(terminalSearch = "Ilica").visibleTerminals)
    }

    @Test
    fun terminalWithoutTidOrOutlet_isHiddenByAnySearch() {
        val bare = MerchantTerminal(30, null, null, null)
        val state = MerchantDetailsUiState(
            content = MerchantDetailsContent.Loaded(merchantDetails(terminals = listOf(bare))),
            terminalSearch = "x"
        )
        assertTrue(state.visibleTerminals.isEmpty())
    }

    @Test
    fun currentSearch_followsTheSelectedTab() {
        val state = loaded(outletSearch = "a", terminalSearch = "b")
        assertEquals("a", state.currentSearch)
        assertEquals("b", state.copy(selectedTab = MerchantDetailsTab.TERMINALS).currentSearch)
    }

    @Test
    fun notLoaded_hasNothingVisible() {
        val state = MerchantDetailsUiState()
        assertTrue(state.visibleOutlets.isEmpty())
        assertTrue(state.visibleTerminals.isEmpty())
    }
}
