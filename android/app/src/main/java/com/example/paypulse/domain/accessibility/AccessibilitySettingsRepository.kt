package com.example.paypulse.domain.accessibility

import kotlinx.coroutines.flow.Flow

interface AccessibilitySettingsRepository {
    val settings: Flow<AccessibilitySettings>
    suspend fun setLargerText(enabled: Boolean)
    suspend fun setHighContrast(enabled: Boolean)
}
