package com.example.paypulse.ui.accessibility

import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.domain.accessibility.AccessibilitySettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class StubAccessibilitySettingsRepository(
    initial: AccessibilitySettings = AccessibilitySettings()
) : AccessibilitySettingsRepository {

    val state = MutableStateFlow(initial)
    val largerTextCalls = mutableListOf<Boolean>()
    val highContrastCalls = mutableListOf<Boolean>()

    override val settings = state

    override suspend fun setLargerText(enabled: Boolean) {
        largerTextCalls += enabled
        state.update { it.copy(largerText = enabled) }
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        highContrastCalls += enabled
        state.update { it.copy(highContrast = enabled) }
    }
}
