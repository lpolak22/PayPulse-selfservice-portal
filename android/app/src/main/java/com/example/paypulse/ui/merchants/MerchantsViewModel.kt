package com.example.paypulse.ui.merchants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantPage
import com.example.paypulse.domain.merchant.MerchantQuery
import com.example.paypulse.domain.merchant.MerchantRepository
import com.example.paypulse.domain.merchant.MerchantResult
import com.example.paypulse.domain.merchant.MerchantStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MILLIS = 300L

class MerchantsViewModel(private val repository: MerchantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantsUiState())
    val uiState: StateFlow<MerchantsUiState> = _uiState.asStateFlow()

    private var merchants: List<Merchant> = emptyList()
    private var nextPage = 0
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        reload()
    }

    fun onEvent(event: MerchantsEvent) {
        when (event) {
            is MerchantsEvent.SearchChanged -> onSearchChanged(event.text)
            MerchantsEvent.SearchCleared -> applyFilters(searchText = "", status = _uiState.value.status)
            is MerchantsEvent.StatusSelected -> applyFilters(_uiState.value.searchText, event.status)
            MerchantsEvent.ClearFilters -> applyFilters(searchText = "", status = null)
            MerchantsEvent.Retry -> retry()
            MerchantsEvent.LoadMore -> loadMore()
            MerchantsEvent.MenuClicked, is MerchantsEvent.MerchantClicked -> Unit
        }
    }

    private fun onSearchChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            reload()
        }
    }

    private fun applyFilters(searchText: String, status: MerchantStatus?) {
        searchJob?.cancel()
        _uiState.update { it.copy(searchText = searchText, status = status) }
        reload()
    }

    private fun retry() {
        val content = _uiState.value.content
        if (content is MerchantsContent.Loaded && content.paging == MerchantsPaging.APPEND_ERROR) loadNextPage(content)
        else reload()
    }

    private fun loadMore() {
        val content = _uiState.value.content as? MerchantsContent.Loaded ?: return
        if (content.paging == MerchantsPaging.IDLE) loadNextPage(content)
    }

    private fun loadNextPage(content: MerchantsContent.Loaded) {
        _uiState.update { it.copy(content = content.copy(paging = MerchantsPaging.LOADING_MORE)) }
        loadPage(nextPage)
    }

    private fun reload() {
        merchants = emptyList()
        nextPage = 0
        _uiState.update { it.copy(content = MerchantsContent.Loading) }
        loadPage(0)
    }

    private fun loadPage(page: Int) {
        loadJob?.cancel()
        val state = _uiState.value
        val query = MerchantQuery(search = state.searchText, status = state.status, page = page)
        loadJob = viewModelScope.launch {
            when (val result = repository.search(query)) {
                is MerchantResult.Success -> onPageLoaded(result.page)
                is MerchantResult.Failure -> onPageFailed(page, result.error)
            }
        }
    }

    private fun onPageLoaded(page: MerchantPage) {
        merchants = (merchants + page.items).distinctBy { it.id }
        nextPage = page.page + 1
        val content = if (merchants.isEmpty()) MerchantsContent.Empty else MerchantsContent.Loaded(
            merchants = merchants,
            total = page.total,
            paging = if (page.hasMore) MerchantsPaging.IDLE else MerchantsPaging.END
        )
        _uiState.update { it.copy(content = content) }
    }

    private fun onPageFailed(page: Int, error: MerchantError) {
        _uiState.update { state ->
            val content = state.content
            if (page > 0 && content is MerchantsContent.Loaded) {
                state.copy(content = content.copy(paging = MerchantsPaging.APPEND_ERROR))
            } else {
                state.copy(content = MerchantsContent.Error(error))
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MerchantsViewModel(AppContainer.merchantRepository) }
        }
    }
}
