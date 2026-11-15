package com.example.paypulse.ui.merchants

import androidx.lifecycle.SavedStateHandle
import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantResult
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.ui.merchants.StubMerchantRepository.Companion.merchants
import com.example.paypulse.ui.merchants.StubMerchantRepository.Companion.successPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MerchantsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubMerchantRepository()
    private val savedState = SavedStateHandle()

    private fun TestScope.openedViewModel(): MerchantsViewModel =
        MerchantsViewModel(repository, savedState).also { advanceUntilIdle() }

    private fun MerchantsViewModel.loaded(): MerchantsContent.Loaded =
        uiState.value.content as MerchantsContent.Loaded

    @Test
    fun opening_showsLoadingThenTheFirstPage() = runTest {
        val viewModel = MerchantsViewModel(repository, savedState)
        assertEquals(MerchantsContent.Loading, viewModel.uiState.value.content)
        advanceUntilIdle()

        assertEquals(merchants(1..2), viewModel.loaded().merchants)
        assertEquals(2L, viewModel.loaded().total)
        assertEquals(MerchantsPaging.END, viewModel.loaded().paging)
        assertEquals(0, repository.queries.single().page)
    }

    @Test
    fun noMerchants_showsEmpty() = runTest {
        repository.defaultResult = successPage(emptyList(), total = 0)
        assertEquals(MerchantsContent.Empty, openedViewModel().uiState.value.content)
    }

    @Test
    fun failedFirstPage_showsTheError() = runTest {
        repository.defaultResult = MerchantResult.Failure(MerchantError.NETWORK)
        assertEquals(MerchantsContent.Error(MerchantError.NETWORK), openedViewModel().uiState.value.content)
    }

    @Test
    fun typing_searchesOnlyAfterThePause() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantsEvent.SearchChanged("kon"))
        advanceTimeBy(100)
        viewModel.onEvent(MerchantsEvent.SearchChanged("konzum"))
        advanceTimeBy(299)
        assertEquals(1, repository.queries.size)

        advanceUntilIdle()
        assertEquals(2, repository.queries.size)
        assertEquals("konzum", repository.queries.last().search)
        assertEquals("konzum", viewModel.uiState.value.searchText)
    }

    @Test
    fun selectingAStatus_reloadsTheFirstPageWithSearchAndStatus() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantsEvent.SearchChanged("konzum"))
        viewModel.onEvent(MerchantsEvent.StatusSelected(MerchantStatus.HOLD))
        advanceUntilIdle()

        val query = repository.queries.last()
        assertEquals("konzum", query.search)
        assertEquals(MerchantStatus.HOLD, query.status)
        assertEquals(0, query.page)
        assertEquals(2, repository.queries.size)
    }

    @Test
    fun clearFilters_resetsSearchAndStatus() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantsEvent.SearchChanged("konzum"))
        viewModel.onEvent(MerchantsEvent.StatusSelected(MerchantStatus.HOLD))
        viewModel.onEvent(MerchantsEvent.ClearFilters)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasFilters)
        assertEquals("", repository.queries.last().search)
        assertEquals(null, repository.queries.last().status)
    }

    @Test
    fun loadMore_appendsTheNextPage() = runTest {
        repository.enqueue(
            successPage(merchants(1..20), total = 25),
            successPage(merchants(21..25), total = 25, page = 1)
        )
        val viewModel = openedViewModel()
        assertEquals(MerchantsPaging.IDLE, viewModel.loaded().paging)

        viewModel.onEvent(MerchantsEvent.LoadMore)
        advanceUntilIdle()

        assertEquals(merchants(1..25), viewModel.loaded().merchants)
        assertEquals(MerchantsPaging.END, viewModel.loaded().paging)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun failedNextPage_keepsTheListAndRetryLoadsItAgain() = runTest {
        repository.enqueue(
            successPage(merchants(1..20), total = 25),
            MerchantResult.Failure(MerchantError.NETWORK),
            successPage(merchants(21..25), total = 25, page = 1)
        )
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantsEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(MerchantsPaging.APPEND_ERROR, viewModel.loaded().paging)
        assertEquals(merchants(1..20), viewModel.loaded().merchants)

        viewModel.onEvent(MerchantsEvent.Retry)
        advanceUntilIdle()
        assertEquals(merchants(1..25), viewModel.loaded().merchants)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun refresh_keepsTheListVisibleAndReplacesIt() = runTest {
        val viewModel = openedViewModel()
        repository.defaultResult = successPage(merchants(3..3), total = 1)

        viewModel.onEvent(MerchantsEvent.Refresh)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(merchants(1..2), viewModel.loaded().merchants)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(merchants(3..3), viewModel.loaded().merchants)
        assertEquals(0, repository.queries.last().page)
    }

    @Test
    fun searchAndStatus_areRestoredFromSavedState() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(MerchantsEvent.SearchChanged("konzum"))
        viewModel.onEvent(MerchantsEvent.StatusSelected(MerchantStatus.DEACTIVATED))
        advanceUntilIdle()

        val restored = openedViewModel()

        assertEquals("konzum", restored.uiState.value.searchText)
        assertEquals(MerchantStatus.DEACTIVATED, restored.uiState.value.status)
        assertEquals("konzum", repository.queries.last().search)
        assertEquals(MerchantStatus.DEACTIVATED, repository.queries.last().status)
    }
}
