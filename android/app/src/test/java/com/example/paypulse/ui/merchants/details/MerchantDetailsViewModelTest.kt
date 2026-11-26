package com.example.paypulse.ui.merchants.details

import androidx.lifecycle.SavedStateHandle
import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantDetailsResult
import com.example.paypulse.ui.merchants.StubMerchantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MerchantDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubMerchantRepository().apply {
        defaultDetailsResult = MerchantDetailsResult.Success(merchantDetails())
    }
    private val savedState = SavedStateHandle(mapOf(MERCHANT_ID_KEY to 7L))

    private fun TestScope.openedViewModel(state: SavedStateHandle = savedState): MerchantDetailsViewModel =
        MerchantDetailsViewModel(repository, state).also { advanceUntilIdle() }

    @Test
    fun opening_showsLoadingThenTheMerchantFromTheRoute() = runTest {
        val viewModel = MerchantDetailsViewModel(repository, savedState)
        assertEquals(MerchantDetailsContent.Loading, viewModel.uiState.value.content)
        advanceUntilIdle()

        assertEquals(MerchantDetailsContent.Loaded(merchantDetails()), viewModel.uiState.value.content)
        assertEquals(listOf(7L), repository.detailsRequests)
        assertEquals(MerchantDetailsTab.OUTLETS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun merchantOfAnotherOrganisation_showsNotFound() = runTest {
        repository.defaultDetailsResult = MerchantDetailsResult.Failure(MerchantDetailsError.NOT_FOUND)
        val content = openedViewModel().uiState.value.content
        assertEquals(MerchantDetailsContent.Error(MerchantDetailsError.NOT_FOUND), content)
    }

    @Test
    fun missingMerchantId_showsNotFoundWithoutCallingTheApi() = runTest {
        val content = openedViewModel(SavedStateHandle()).uiState.value.content
        assertEquals(MerchantDetailsContent.Error(MerchantDetailsError.NOT_FOUND), content)
        assertTrue(repository.detailsRequests.isEmpty())
    }

    @Test
    fun retryAfterNetworkError_loadsAgain() = runTest {
        repository.enqueueDetails(MerchantDetailsResult.Failure(MerchantDetailsError.NETWORK))
        val viewModel = openedViewModel()
        assertEquals(MerchantDetailsContent.Error(MerchantDetailsError.NETWORK), viewModel.uiState.value.content)

        viewModel.onEvent(MerchantDetailsEvent.Retry)
        advanceUntilIdle()

        assertEquals(MerchantDetailsContent.Loaded(merchantDetails()), viewModel.uiState.value.content)
        assertEquals(listOf(7L, 7L), repository.detailsRequests)
    }

    @Test
    fun searchText_isKeptPerTab() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantDetailsEvent.SearchChanged("Ilica"))
        viewModel.onEvent(MerchantDetailsEvent.TabSelected(MerchantDetailsTab.TERMINALS))
        viewModel.onEvent(MerchantDetailsEvent.SearchChanged("TK0002"))

        val state = viewModel.uiState.value
        assertEquals("Ilica", state.outletSearch)
        assertEquals("TK0002", state.terminalSearch)
        assertEquals(listOf(ilica), state.visibleOutlets)
        assertEquals(listOf(mallTerminal), state.visibleTerminals)
    }

    @Test
    fun clearingSearch_onlyClearsTheSelectedTab() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantDetailsEvent.SearchChanged("Ilica"))
        viewModel.onEvent(MerchantDetailsEvent.TabSelected(MerchantDetailsTab.TERMINALS))
        viewModel.onEvent(MerchantDetailsEvent.SearchChanged("TK"))
        viewModel.onEvent(MerchantDetailsEvent.SearchCleared)

        assertEquals("Ilica", viewModel.uiState.value.outletSearch)
        assertEquals("", viewModel.uiState.value.terminalSearch)
    }

    @Test
    fun tabAndSearch_surviveRecreation() = runTest {
        openedViewModel().apply {
            onEvent(MerchantDetailsEvent.SearchChanged("Gorica"))
            onEvent(MerchantDetailsEvent.TabSelected(MerchantDetailsTab.TERMINALS))
        }

        val restored = openedViewModel().uiState.value
        assertEquals(MerchantDetailsTab.TERMINALS, restored.selectedTab)
        assertEquals("Gorica", restored.outletSearch)
    }

    @Test
    fun navigationEvents_doNotChangeState() = runTest {
        val viewModel = openedViewModel()
        val before = viewModel.uiState.value
        listOf(
            MerchantDetailsEvent.BackClicked,
            MerchantDetailsEvent.MerchantsClicked,
            MerchantDetailsEvent.OutletClicked(11),
            MerchantDetailsEvent.TerminalClicked(21)
        ).forEach(viewModel::onEvent)
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value)
        assertEquals(1, repository.detailsRequests.size)
    }
}
