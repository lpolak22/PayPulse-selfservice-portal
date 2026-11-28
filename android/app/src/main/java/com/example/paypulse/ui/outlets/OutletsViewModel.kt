package com.example.paypulse.ui.outlets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletCitiesResult
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletPage
import com.example.paypulse.domain.outlet.OutletQuery
import com.example.paypulse.domain.outlet.OutletRepository
import com.example.paypulse.domain.outlet.OutletResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val SEARCH_KEY = "outlets_search"
private const val CITY_KEY = "outlets_city"

class OutletsViewModel(
    private val repository: OutletRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val merchantId: Long = requireNotNull(savedStateHandle[MERCHANT_ID_KEY]) { "Missing $MERCHANT_ID_KEY" }

    private val _uiState = MutableStateFlow(
        OutletsUiState(
            merchantName = savedStateHandle[MERCHANT_NAME_KEY] ?: "",
            searchText = savedStateHandle[SEARCH_KEY] ?: "",
            city = savedStateHandle[CITY_KEY]
        )
    )
    val uiState: StateFlow<OutletsUiState> = _uiState.asStateFlow()

    private var outlets: List<Outlet> = emptyList()
    private var loadedQuery: OutletQuery = currentQuery()
    private var nextPage = 0
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var citiesJob: Job? = null

    init {
        loadCities()
        reload()
    }

    fun onEvent(event: OutletsEvent) {
        when (event) {
            is OutletsEvent.SearchChanged -> onSearchChanged(event.text)
            OutletsEvent.SearchCleared -> applyFilters(searchText = "", city = _uiState.value.city)
            is OutletsEvent.CitySelected -> applyFilters(_uiState.value.searchText, event.city)
            OutletsEvent.ClearFilters -> applyFilters(searchText = "", city = null)
            OutletsEvent.Retry -> retry()
            OutletsEvent.Refresh -> refresh()
            OutletsEvent.LoadMore -> loadMore()
            OutletsEvent.BackClicked, is OutletsEvent.OutletClicked -> Unit
        }
    }

    private fun onSearchChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        savedStateHandle[SEARCH_KEY] = text
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            reload()
        }
    }

    private fun applyFilters(searchText: String, city: String?) {
        searchJob?.cancel()
        _uiState.update { it.copy(searchText = searchText, city = city) }
        savedStateHandle[SEARCH_KEY] = searchText
        savedStateHandle[CITY_KEY] = city
        reload()
    }

    private fun retry() {
        val content = _uiState.value.content
        if (content is OutletsContent.Loaded && content.paging == OutletsPaging.APPEND_ERROR) loadNextPage(content)
        else reload()
        if (_uiState.value.cities.isEmpty()) loadCities()
    }

    private fun refresh() {
        searchJob?.cancel()
        _uiState.update { it.copy(isRefreshing = true, refreshError = null) }
        loadCities()
        loadedQuery = currentQuery()
        loadPage(loadedQuery)
    }

    private fun loadMore() {
        val state = _uiState.value
        val content = state.content as? OutletsContent.Loaded ?: return
        val searchPending = searchJob?.isActive == true
        if (content.paging == OutletsPaging.IDLE && !state.isRefreshing && !searchPending) loadNextPage(content)
    }

    private fun loadNextPage(content: OutletsContent.Loaded) {
        _uiState.update { it.copy(content = content.copy(paging = OutletsPaging.LOADING_MORE)) }
        loadPage(loadedQuery.copy(page = nextPage))
    }

    private fun reload() {
        outlets = emptyList()
        nextPage = 0
        _uiState.update { it.copy(content = OutletsContent.Loading, refreshError = null) }
        loadedQuery = currentQuery()
        loadPage(loadedQuery)
    }

    private fun currentQuery(): OutletQuery {
        val state = _uiState.value
        return OutletQuery(merchantId = merchantId, search = state.searchText, city = state.city)
    }

    private fun loadCities() {
        citiesJob?.cancel()
        citiesJob = viewModelScope.launch {
            val result = repository.cities(merchantId)
            if (result is OutletCitiesResult.Success) _uiState.update { it.copy(cities = result.cities) }
        }
    }

    private fun loadPage(query: OutletQuery) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.search(query)) {
                is OutletResult.Success -> onPageLoaded(result.page)
                is OutletResult.Failure -> onPageFailed(query.page, result.error)
            }
        }
    }

    private fun onPageLoaded(page: OutletPage) {
        outlets = if (page.page == 0) page.items else (outlets + page.items).distinctBy { it.id }
        nextPage = page.page + 1
        val content = if (outlets.isEmpty()) OutletsContent.Empty else OutletsContent.Loaded(
            outlets = outlets,
            total = page.total,
            paging = if (page.hasMore) OutletsPaging.IDLE else OutletsPaging.END
        )
        _uiState.update { it.copy(content = content, isRefreshing = false, refreshError = null) }
    }

    private fun onPageFailed(page: Int, error: OutletError) {
        _uiState.update { state ->
            val content = state.content
            when {
                content !is OutletsContent.Loaded -> state.copy(content = OutletsContent.Error(error), isRefreshing = false)
                page > 0 -> state.copy(content = content.copy(paging = OutletsPaging.APPEND_ERROR), isRefreshing = false)
                else -> state.copy(isRefreshing = false, refreshError = error)
            }
        }
    }

    companion object {
        const val MERCHANT_ID_KEY = "merchantId"
        const val MERCHANT_NAME_KEY = "merchantName"

        val Factory = viewModelFactory {
            initializer { OutletsViewModel(AppContainer.outletRepository, createSavedStateHandle()) }
        }
    }
}
