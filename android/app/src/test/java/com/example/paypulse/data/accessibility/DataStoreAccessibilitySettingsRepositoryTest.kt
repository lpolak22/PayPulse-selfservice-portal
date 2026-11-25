package com.example.paypulse.data.accessibility

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAccessibilitySettingsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val file: File by lazy { File(tmpFolder.root, "accessibility_settings.preferences_pb") }

    private fun dataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    private fun TestScope.repository(): DataStoreAccessibilitySettingsRepository =
        DataStoreAccessibilitySettingsRepository(dataStore(backgroundScope))

    @Test
    fun initialSettings_areAllOff() = runTest {
        assertEquals(AccessibilitySettings(largerText = false, highContrast = false), repository().settings.first())
    }

    @Test
    fun setLargerText_turnsOnOnlyLargerText() = runTest {
        val repository = repository()

        repository.setLargerText(true)

        assertEquals(AccessibilitySettings(largerText = true, highContrast = false), repository.settings.first())
    }

    @Test
    fun setHighContrast_turnsOnOnlyHighContrast() = runTest {
        val repository = repository()

        repository.setHighContrast(true)

        assertEquals(AccessibilitySettings(largerText = false, highContrast = true), repository.settings.first())
    }

    @Test
    fun bothSettingsOn_flowReturnsBothTrue() = runTest {
        val repository = repository()

        repository.setLargerText(true)
        repository.setHighContrast(true)

        assertEquals(AccessibilitySettings(largerText = true, highContrast = true), repository.settings.first())
    }

    @Test
    fun turningSettingOff_returnsItToFalse() = runTest {
        val repository = repository()
        repository.setLargerText(true)
        repository.setHighContrast(true)

        repository.setHighContrast(false)

        assertEquals(AccessibilitySettings(largerText = true, highContrast = false), repository.settings.first())
    }

    @Test
    fun settings_areStoredUnderTheAgreedKeys() = runTest {
        val dataStore = dataStore(backgroundScope)
        val repository = DataStoreAccessibilitySettingsRepository(dataStore)

        repository.setLargerText(true)

        val preferences = dataStore.data.first()
        assertEquals(true, preferences[booleanPreferencesKey("larger_text")])
        assertNull(preferences[booleanPreferencesKey("high_contrast")])
    }

    @Test
    fun settings_surviveAppRestart() = runTest {
        val firstRunScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        val firstRun = DataStoreAccessibilitySettingsRepository(dataStore(firstRunScope))
        firstRun.setLargerText(true)
        firstRun.setHighContrast(true)
        firstRunScope.coroutineContext.job.cancelAndJoin()

        val secondRun = repository()

        assertEquals(AccessibilitySettings(largerText = true, highContrast = true), secondRun.settings.first())
    }
}
