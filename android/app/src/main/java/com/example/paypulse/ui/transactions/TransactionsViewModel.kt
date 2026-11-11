package com.example.paypulse.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.transaction.Transaction
import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.domain.transaction.TransactionPage
import com.example.paypulse.domain.transaction.TransactionQuery
import com.example.paypulse.domain.transaction.TransactionRepository
import com.example.paypulse.domain.transaction.TransactionResult
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.withSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

private const val STATE_KEY = "transactions_state"

class TransactionsViewModel(
    private val repository: TransactionRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val defaultRange = DashboardPeriod.LAST_7_DAYS.toDateRange(clock)
    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private var transactions: List<Transaction> = emptyList()
    private var nextPage = 0
    private var loadJob: Job? = null

    init {
        reload()
    }

    fun onEvent(event: TransactionsEvent) {
        when (event) {
            is FilterSheetEvent -> onFilterSheetEvent(event)
            TransactionsEvent.Retry -> retry()
            TransactionsEvent.LoadMore -> loadMore()
            TransactionsEvent.ClearFilters -> applyFilters(TransactionFilters(defaultRange), searchText = "")
            is TransactionsEvent.SearchChanged -> _uiState.update { it.copy(searchText = event.text) }
            TransactionsEvent.SearchSubmitted -> submitSearch()
            TransactionsEvent.SearchCleared -> applyFilters(_uiState.value.filters.withSearch(""), searchText = "")
            is TransactionsEvent.ActiveFilterRemoved -> removeFilter(event.filter)
            TransactionsEvent.FilterButtonClicked -> openFilterSheet()
            TransactionsEvent.MenuClicked, is TransactionsEvent.TransactionClicked -> Unit
        }
    }

    private fun onFilterSheetEvent(event: FilterSheetEvent) {
        when (event) {
            FilterSheetEvent.Dismissed -> _uiState.update { it.copy(filterDraft = null) }
            is FilterSheetEvent.OptionToggled -> updateDraft { it.copy(filters = it.filters.toggle(event.option)) }
            is FilterSheetEvent.DatesChanged -> updateDraft { it.copy(filters = it.filters.copy(dateRange = event.range)) }
            is FilterSheetEvent.AuthCodeChanged -> updateDraft { it.copy(filters = it.filters.copy(authCode = event.text)) }
            is FilterSheetEvent.RrnChanged -> updateDraft { it.copy(filters = it.filters.copy(rrn = event.text)) }
            is FilterSheetEvent.SortChanged -> updateDraft { it.copy(sort = event.sort) }
            FilterSheetEvent.ClearAll -> updateDraft { it.copy(filters = TransactionFilters(defaultRange)) }
            FilterSheetEvent.Apply -> applyDraft()
        }
    }

    private fun initialState(): TransactionsUiState {
        val restored = savedStateHandle.get<String>(STATE_KEY)?.let(::decodeTransactionsState)
        val filters = restored?.filters ?: TransactionFilters(defaultRange)
        return TransactionsUiState(
            filters = filters,
            sort = restored?.sort ?: TransactionSort.DEFAULT,
            searchText = restored?.searchText.orEmpty(),
            activeFilters = filters.activeFilters(defaultRange)
        )
    }

    private fun openFilterSheet() {
        _uiState.update { it.copy(filterDraft = FilterDraft(it.filters, it.sort, today())) }
    }

    private fun updateDraft(transform: (FilterDraft) -> FilterDraft) {
        _uiState.update { state -> state.filterDraft?.let { state.copy(filterDraft = transform(it)) } ?: state }
    }

    private fun applyDraft() {
        val draft = _uiState.value.filterDraft ?: return
        applyFilters(draft.filters.normalized(), draft.sort, searchText = "")
    }

    private fun submitSearch() {
        val state = _uiState.value
        applyFilters(state.filters.withSearch(state.searchText), searchText = state.searchText.trim())
    }

    private fun removeFilter(filter: ActiveFilter) {
        val state = _uiState.value
        val searchText = if (filter.matchesSearch(state.searchText)) "" else state.searchText
        applyFilters(state.filters.without(filter, defaultRange), searchText = searchText)
    }

    private fun ActiveFilter.matchesSearch(text: String): Boolean = when (this) {
        is ActiveFilter.AuthCode -> value == text.trim()
        is ActiveFilter.Rrn -> value == text.trim()
        else -> false
    }

    private fun applyFilters(
        filters: TransactionFilters,
        sort: TransactionSort = _uiState.value.sort,
        searchText: String = _uiState.value.searchText
    ) {
        val previous = _uiState.value
        _uiState.update {
            it.copy(
                filters = filters,
                sort = sort,
                searchText = searchText,
                activeFilters = filters.activeFilters(defaultRange),
                filterDraft = null
            )
        }
        savedStateHandle[STATE_KEY] = encodeTransactionsState(filters, sort, searchText)
        val changed = filters != previous.filters || sort != previous.sort
        if (changed || previous.content is TransactionsContent.Error) reload()
    }

    private fun retry() {
        val content = _uiState.value.content
        if (content is TransactionsContent.Loaded && content.paging == PagingStatus.APPEND_ERROR) loadNextPage(content)
        else reload()
    }

    private fun loadMore() {
        val content = _uiState.value.content as? TransactionsContent.Loaded ?: return
        if (content.paging == PagingStatus.IDLE) loadNextPage(content)
    }

    private fun loadNextPage(content: TransactionsContent.Loaded) {
        _uiState.update { it.copy(content = content.copy(paging = PagingStatus.LOADING_MORE)) }
        loadPage(nextPage)
    }

    private fun reload() {
        transactions = emptyList()
        nextPage = 0
        _uiState.update { it.copy(content = TransactionsContent.Loading) }
        loadPage(0)
    }

    private fun loadPage(page: Int) {
        loadJob?.cancel()
        val state = _uiState.value
        val query = TransactionQuery(filters = state.filters, sort = state.sort, page = page)
        loadJob = viewModelScope.launch {
            when (val result = repository.search(query)) {
                is TransactionResult.Success -> onPageLoaded(result.page, state.sort)
                is TransactionResult.Failure -> onPageFailed(page, result.error)
            }
        }
    }

    private fun onPageLoaded(page: TransactionPage, sort: TransactionSort) {
        transactions = (transactions + page.items).distinctBy { it.id }
        nextPage = page.page + 1
        val content = if (transactions.isEmpty()) TransactionsContent.Empty else TransactionsContent.Loaded(
            items = transactions.toListItems(sort, today()),
            total = page.total,
            paging = if (page.hasMore) PagingStatus.IDLE else PagingStatus.END
        )
        _uiState.update { it.copy(content = content) }
    }

    private fun onPageFailed(page: Int, error: TransactionError) {
        _uiState.update { state ->
            val content = state.content
            if (page > 0 && content is TransactionsContent.Loaded) {
                state.copy(content = content.copy(paging = PagingStatus.APPEND_ERROR))
            } else {
                state.copy(content = TransactionsContent.Error(error))
            }
        }
    }

    private fun today(): LocalDate = LocalDate.now(clock.withZone(DashboardPeriod.ZONE))

    companion object {
        val Factory = viewModelFactory {
            initializer {
                TransactionsViewModel(AppContainer.transactionRepository, AppContainer.clock, createSavedStateHandle())
            }
        }
    }
}
