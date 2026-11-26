package com.example.paypulse.ui.terminals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalPage
import com.example.paypulse.domain.terminal.TerminalQuery
import com.example.paypulse.domain.terminal.TerminalRepository
import com.example.paypulse.domain.terminal.TerminalResult
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATUS_KEY = "terminals_status"
private const val TYPE_KEY = "terminals_type"

class TerminalsViewModel(
    private val repository: TerminalRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TerminalsUiState(
            status = savedStateHandle.get<String>(STATUS_KEY)?.let { name -> TerminalStatus.entries.find { it.name == name } },
            type = savedStateHandle.get<String>(TYPE_KEY)?.let { name -> TerminalType.entries.find { it.name == name } }
        )
    )
    val uiState: StateFlow<TerminalsUiState> = _uiState.asStateFlow()

    private var terminals: List<Terminal> = emptyList()
    private var nextPage = 0
    private var loadJob: Job? = null

    init {
        reload()
    }

    fun onEvent(event: TerminalsEvent) {
        when (event) {
            is TerminalsEvent.StatusSelected -> applyFilters(event.status, _uiState.value.type)
            is TerminalsEvent.TypeSelected -> applyFilters(_uiState.value.status, event.type)
            TerminalsEvent.ClearFilters -> applyFilters(status = null, type = null)
            TerminalsEvent.Retry -> retry()
            TerminalsEvent.Refresh -> refresh()
            TerminalsEvent.LoadMore -> loadMore()
            TerminalsEvent.MenuClicked, is TerminalsEvent.TerminalClicked -> Unit
        }
    }

    private fun applyFilters(status: TerminalStatus?, type: TerminalType?) {
        _uiState.update { it.copy(status = status, type = type) }
        savedStateHandle[STATUS_KEY] = status?.name
        savedStateHandle[TYPE_KEY] = type?.name
        reload()
    }

    private fun retry() {
        val content = _uiState.value.content
        if (content is TerminalsContent.Loaded && content.paging == TerminalsPaging.APPEND_ERROR) loadNextPage(content)
        else reload()
    }

    private fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPage(0)
    }

    private fun loadMore() {
        val state = _uiState.value
        val content = state.content as? TerminalsContent.Loaded ?: return
        if (content.paging == TerminalsPaging.IDLE && !state.isRefreshing) loadNextPage(content)
    }

    private fun loadNextPage(content: TerminalsContent.Loaded) {
        _uiState.update { it.copy(content = content.copy(paging = TerminalsPaging.LOADING_MORE)) }
        loadPage(nextPage)
    }

    private fun reload() {
        terminals = emptyList()
        nextPage = 0
        _uiState.update { it.copy(content = TerminalsContent.Loading) }
        loadPage(0)
    }

    private fun loadPage(page: Int) {
        loadJob?.cancel()
        val state = _uiState.value
        val query = TerminalQuery(status = state.status, type = state.type, page = page)
        loadJob = viewModelScope.launch {
            when (val result = repository.search(query)) {
                is TerminalResult.Success -> onPageLoaded(result.page)
                is TerminalResult.Failure -> onPageFailed(page, result.error)
            }
        }
    }

    private fun onPageLoaded(page: TerminalPage) {
        terminals = if (page.page == 0) page.items else (terminals + page.items).distinctBy { it.id }
        nextPage = page.page + 1
        val content = if (terminals.isEmpty()) TerminalsContent.Empty else TerminalsContent.Loaded(
            terminals = terminals,
            total = page.total,
            paging = if (page.hasMore) TerminalsPaging.IDLE else TerminalsPaging.END
        )
        _uiState.update { it.copy(content = content, isRefreshing = false) }
    }

    private fun onPageFailed(page: Int, error: TerminalError) {
        _uiState.update { state ->
            val content = state.content
            if (page > 0 && content is TerminalsContent.Loaded) {
                state.copy(content = content.copy(paging = TerminalsPaging.APPEND_ERROR), isRefreshing = false)
            } else {
                state.copy(content = TerminalsContent.Error(error), isRefreshing = false)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TerminalsViewModel(AppContainer.terminalRepository, createSavedStateHandle()) }
        }
    }
}
