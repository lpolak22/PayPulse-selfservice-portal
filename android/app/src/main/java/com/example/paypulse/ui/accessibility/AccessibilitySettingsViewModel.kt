package com.example.paypulse.ui.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.domain.accessibility.AccessibilitySettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccessibilitySettingsViewModel(
    private val repository: AccessibilitySettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AccessibilitySettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccessibilitySettings()
    )

    fun onLargerTextChange(enabled: Boolean) {
        viewModelScope.launch { repository.setLargerText(enabled) }
    }

    fun onHighContrastChange(enabled: Boolean) {
        viewModelScope.launch { repository.setHighContrast(enabled) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AccessibilitySettingsViewModel(AppContainer.accessibilitySettingsRepository) }
        }
    }
}
