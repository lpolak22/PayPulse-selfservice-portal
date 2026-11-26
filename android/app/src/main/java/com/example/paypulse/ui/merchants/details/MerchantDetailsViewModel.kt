package com.example.paypulse.ui.merchants.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantDetailsResult
import com.example.paypulse.domain.merchant.MerchantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MERCHANT_ID_KEY = "id"
private const val TAB_KEY = "merchant_details_tab"
private const val OUTLET_SEARCH_KEY = "merchant_details_outlet_search"
private const val TERMINAL_SEARCH_KEY = "merchant_details_terminal_search"

class MerchantDetailsViewModel(
    private val repository: MerchantRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val merchantId: Long? = savedStateHandle[MERCHANT_ID_KEY]

    private val _uiState = MutableStateFlow(
        MerchantDetailsUiState(
            selectedTab = savedStateHandle.get<String>(TAB_KEY)
                ?.let { name -> MerchantDetailsTab.entries.find { it.name == name } }
                ?: MerchantDetailsTab.OUTLETS,
            outletSearch = savedStateHandle[OUTLET_SEARCH_KEY] ?: "",
            terminalSearch = savedStateHandle[TERMINAL_SEARCH_KEY] ?: ""
        )
    )
    val uiState: StateFlow<MerchantDetailsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onEvent(event: MerchantDetailsEvent) {
        when (event) {
            is MerchantDetailsEvent.TabSelected -> selectTab(event.tab)
            is MerchantDetailsEvent.SearchChanged -> changeSearch(event.text)
            MerchantDetailsEvent.SearchCleared -> changeSearch("")
            MerchantDetailsEvent.Retry -> load()
            MerchantDetailsEvent.BackClicked,
            MerchantDetailsEvent.MerchantsClicked,
            is MerchantDetailsEvent.OutletClicked,
            is MerchantDetailsEvent.TerminalClicked -> Unit
        }
    }

    private fun selectTab(tab: MerchantDetailsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        savedStateHandle[TAB_KEY] = tab.name
    }

    private fun changeSearch(text: String) {
        when (_uiState.value.selectedTab) {
            MerchantDetailsTab.OUTLETS -> {
                _uiState.update { it.copy(outletSearch = text) }
                savedStateHandle[OUTLET_SEARCH_KEY] = text
            }
            MerchantDetailsTab.TERMINALS -> {
                _uiState.update { it.copy(terminalSearch = text) }
                savedStateHandle[TERMINAL_SEARCH_KEY] = text
            }
        }
    }

    private fun load() {
        val id = merchantId
        if (id == null) {
            _uiState.update { it.copy(content = MerchantDetailsContent.Error(MerchantDetailsError.NOT_FOUND)) }
            return
        }
        loadJob?.cancel()
        _uiState.update { it.copy(content = MerchantDetailsContent.Loading) }
        loadJob = viewModelScope.launch {
            val content = when (val result = repository.details(id)) {
                is MerchantDetailsResult.Success -> MerchantDetailsContent.Loaded(result.details)
                is MerchantDetailsResult.Failure -> MerchantDetailsContent.Error(result.error)
            }
            _uiState.update { it.copy(content = content) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MerchantDetailsViewModel(AppContainer.merchantRepository, createSavedStateHandle()) }
        }
    }
}
