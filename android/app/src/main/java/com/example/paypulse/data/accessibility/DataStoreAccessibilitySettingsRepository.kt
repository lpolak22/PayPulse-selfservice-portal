package com.example.paypulse.data.accessibility

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.domain.accessibility.AccessibilitySettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.accessibilitySettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "accessibility_settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)
private val LARGER_TEXT_KEY = booleanPreferencesKey("larger_text")
private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")

class DataStoreAccessibilitySettingsRepository(
    private val dataStore: DataStore<Preferences>
) : AccessibilitySettingsRepository {

    constructor(context: Context) : this(context.accessibilitySettingsDataStore)

    override val settings: Flow<AccessibilitySettings> = dataStore.data.map { preferences ->
        AccessibilitySettings(
            largerText = preferences[LARGER_TEXT_KEY] ?: false,
            highContrast = preferences[HIGH_CONTRAST_KEY] ?: false
        )
    }

    override suspend fun setLargerText(enabled: Boolean) {
        dataStore.edit { it[LARGER_TEXT_KEY] = enabled }
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { it[HIGH_CONTRAST_KEY] = enabled }
    }
}
