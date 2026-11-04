package com.example.paypulse.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.dashboard.DashboardRepository
import com.example.paypulse.domain.dashboard.DashboardResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

private const val PERIOD_KEY = "period"

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(period = savedStateHandle[PERIOD_KEY] ?: DashboardPeriod.DEFAULT)
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onPeriodSelected(period: DashboardPeriod) {
        if (period == _uiState.value.period) return
        savedStateHandle[PERIOD_KEY] = period
        _uiState.update { it.copy(period = period, selection = DashboardSelection()) }
        load()
    }

    fun onTrendSelected(index: Int) {
        _uiState.update { it.copy(selection = it.selection.copy(trend = index)) }
    }

    fun onBrandSelected(index: Int) {
        _uiState.update { it.copy(selection = it.selection.copy(brand = index)) }
    }

    fun onAcquirerSelected(index: Int) {
        _uiState.update { it.copy(selection = it.selection.copy(acquirer = index)) }
    }

    fun onRetry() {
        load(fromPull = false)
    }

    fun onRefresh() {
        load(fromPull = true)
    }

    private fun load(fromPull: Boolean = false) {
        loadJob?.cancel()
        _uiState.update { it.startLoading(fromPull) }
        val period = _uiState.value.period
        loadJob = viewModelScope.launch {
            val range = period.toDateRange(clock)
            val content = repository.getSummary(range).toContentState(period, today = range.to)
            _uiState.update { it.finishLoading(content) }
        }
    }

    private fun DashboardResult.toContentState(period: DashboardPeriod, today: LocalDate): DashboardContentState =
        when (this) {
            is DashboardResult.Success ->
                if (summary.hasNoTransactions) DashboardContentState.Empty
                else DashboardContentState.Content(summary, period, today)
            is DashboardResult.Failure -> DashboardContentState.Error(error)
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DashboardViewModel(AppContainer.dashboardRepository, AppContainer.clock, createSavedStateHandle())
            }
        }
    }
}
