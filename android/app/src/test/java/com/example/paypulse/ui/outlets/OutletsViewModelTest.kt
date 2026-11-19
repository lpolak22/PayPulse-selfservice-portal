package com.example.paypulse.ui.outlets

import androidx.lifecycle.SavedStateHandle
import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.outlet.OutletCitiesResult
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletResult
import com.example.paypulse.ui.outlets.StubOutletRepository.Companion.outlets
import com.example.paypulse.ui.outlets.StubOutletRepository.Companion.successPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val MERCHANT_ID = 7L

@OptIn(ExperimentalCoroutinesApi::class)
class OutletsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubOutletRepository()
    private val savedState = SavedStateHandle(
        mapOf(OutletsViewModel.MERCHANT_ID_KEY to MERCHANT_ID, OutletsViewModel.MERCHANT_NAME_KEY to "Konzum d.d.")
    )

    private fun TestScope.openedViewModel(): OutletsViewModel =
        OutletsViewModel(repository, savedState).also { advanceUntilIdle() }

    private fun OutletsViewModel.loaded(): OutletsContent.Loaded =
        uiState.value.content as OutletsContent.Loaded

    @Test
    fun opening_showsLoadingThenTheFirstPageOfTheMerchantsOutlets() = runTest {
        val viewModel = OutletsViewModel(repository, savedState)
        assertEquals(OutletsContent.Loading, viewModel.uiState.value.content)
        advanceUntilIdle()

        assertEquals(outlets(1..2), viewModel.loaded().outlets)
        assertEquals(2L, viewModel.loaded().total)
        assertEquals(OutletsPaging.END, viewModel.loaded().paging)
        assertEquals("Konzum d.d.", viewModel.uiState.value.merchantName)
        assertEquals(MERCHANT_ID, repository.queries.single().merchantId)
        assertEquals(0, repository.queries.single().page)
    }

    @Test
    fun opening_loadsTheCityFilterOptionsForTheMerchant() = runTest {
        val viewModel = openedViewModel()

        assertEquals(listOf("Rijeka", "Split", "Zagreb"), viewModel.uiState.value.cities)
        assertEquals(listOf(MERCHANT_ID), repository.cityRequests)
    }

    @Test
    fun citiesThatFailToLoad_leaveTheFilterEmptyAndRetryLoadsThemAgain() = runTest {
        repository.citiesResult = OutletCitiesResult.Failure(OutletError.NETWORK)
        val viewModel = openedViewModel()
        assertEquals(emptyList<String>(), viewModel.uiState.value.cities)

        repository.citiesResult = OutletCitiesResult.Success(listOf("Rijeka"))
        viewModel.onEvent(OutletsEvent.Retry)
        advanceUntilIdle()

        assertEquals(listOf("Rijeka"), viewModel.uiState.value.cities)
    }

    @Test
    fun noOutlets_showsEmpty() = runTest {
        repository.defaultResult = successPage(emptyList(), total = 0)
        assertEquals(OutletsContent.Empty, openedViewModel().uiState.value.content)
    }

    @Test
    fun failedFirstPage_showsTheError() = runTest {
        repository.defaultResult = OutletResult.Failure(OutletError.MERCHANT_NOT_FOUND)
        assertEquals(OutletsContent.Error(OutletError.MERCHANT_NOT_FOUND), openedViewModel().uiState.value.content)
    }

    @Test
    fun typing_searchesOnlyAfterThePause() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(OutletsEvent.SearchChanged("ili"))
        advanceTimeBy(100)
        viewModel.onEvent(OutletsEvent.SearchChanged("ilica"))
        advanceTimeBy(299)
        assertEquals(1, repository.queries.size)

        advanceUntilIdle()
        assertEquals(2, repository.queries.size)
        assertEquals("ilica", repository.queries.last().search)
        assertEquals("ilica", viewModel.uiState.value.searchText)
    }

    @Test
    fun selectingACity_reloadsTheFirstPageWithSearchAndCity() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(OutletsEvent.SearchChanged("konzum"))
        viewModel.onEvent(OutletsEvent.CitySelected("Rijeka"))
        advanceUntilIdle()

        val query = repository.queries.last()
        assertEquals("konzum", query.search)
        assertEquals("Rijeka", query.city)
        assertEquals(0, query.page)
        assertEquals(MERCHANT_ID, query.merchantId)
        assertEquals(2, repository.queries.size)
    }

    @Test
    fun noMatchForTheFilters_showsEmptyWithFiltersStillSet() = runTest {
        val viewModel = openedViewModel()
        repository.defaultResult = successPage(emptyList(), total = 0)
        viewModel.onEvent(OutletsEvent.CitySelected("Rijeka"))
        advanceUntilIdle()

        assertEquals(OutletsContent.Empty, viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.hasFilters)
    }

    @Test
    fun clearFilters_resetsSearchAndCity() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(OutletsEvent.SearchChanged("konzum"))
        viewModel.onEvent(OutletsEvent.CitySelected("Rijeka"))
        viewModel.onEvent(OutletsEvent.ClearFilters)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasFilters)
        assertEquals("", repository.queries.last().search)
        assertNull(repository.queries.last().city)
    }

    @Test
    fun loadMore_appendsTheNextPage() = runTest {
        repository.enqueue(
            successPage(outlets(1..20), total = 25),
            successPage(outlets(21..25), total = 25, page = 1)
        )
        val viewModel = openedViewModel()
        assertEquals(OutletsPaging.IDLE, viewModel.loaded().paging)

        viewModel.onEvent(OutletsEvent.LoadMore)
        advanceUntilIdle()

        assertEquals(outlets(1..25), viewModel.loaded().outlets)
        assertEquals(OutletsPaging.END, viewModel.loaded().paging)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun failedNextPage_keepsTheListAndRetryLoadsItAgain() = runTest {
        repository.enqueue(
            successPage(outlets(1..20), total = 25),
            OutletResult.Failure(OutletError.NETWORK),
            successPage(outlets(21..25), total = 25, page = 1)
        )
        val viewModel = openedViewModel()
        viewModel.onEvent(OutletsEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(OutletsPaging.APPEND_ERROR, viewModel.loaded().paging)
        assertEquals(outlets(1..20), viewModel.loaded().outlets)

        viewModel.onEvent(OutletsEvent.Retry)
        advanceUntilIdle()
        assertEquals(outlets(1..25), viewModel.loaded().outlets)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun refresh_keepsTheListVisibleAndReloadsOutletsAndCities() = runTest {
        val viewModel = openedViewModel()
        repository.defaultResult = successPage(outlets(3..3), total = 1)
        repository.citiesResult = OutletCitiesResult.Success(listOf("Osijek"))

        viewModel.onEvent(OutletsEvent.Refresh)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(outlets(1..2), viewModel.loaded().outlets)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(outlets(3..3), viewModel.loaded().outlets)
        assertEquals(listOf("Osijek"), viewModel.uiState.value.cities)
    }

    @Test
    fun searchAndCity_areRestoredFromSavedState() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(OutletsEvent.SearchChanged("ilica"))
        viewModel.onEvent(OutletsEvent.CitySelected("Zagreb"))
        advanceUntilIdle()

        val restored = openedViewModel()

        assertEquals("ilica", restored.uiState.value.searchText)
        assertEquals("Zagreb", restored.uiState.value.city)
        assertEquals("ilica", repository.queries.last().search)
        assertEquals("Zagreb", repository.queries.last().city)
    }
}
