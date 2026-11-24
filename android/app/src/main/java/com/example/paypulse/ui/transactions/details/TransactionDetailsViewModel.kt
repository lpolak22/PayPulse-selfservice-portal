package com.example.paypulse.ui.transactions.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.domain.transaction.TransactionDetailsRepository
import com.example.paypulse.domain.transaction.TransactionDetailsResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailsViewModel(
    private val transactionId: Long,
    private val repository: TransactionDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionDetailsUiState>(TransactionDetailsUiState.Loading)
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = TransactionDetailsUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = repository.getDetails(transactionId).toUiState()
        }
    }

    private fun TransactionDetailsResult.toUiState(): TransactionDetailsUiState = when (this) {
        is TransactionDetailsResult.Success -> TransactionDetailsUiState.Loaded(details.toDetailsModel())
        is TransactionDetailsResult.Failure ->
            if (error == TransactionDetailsError.NOT_FOUND) TransactionDetailsUiState.NotFound
            else TransactionDetailsUiState.Error(error)
    }

    companion object {
        fun factory(transactionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionDetailsViewModel(transactionId, AppContainer.transactionDetailsRepository) }
        }
    }
}
