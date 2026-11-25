package com.example.paypulse.ui.accessibility

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccessibilitySettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubAccessibilitySettingsRepository()

    private fun TestScope.observedViewModel(): AccessibilitySettingsViewModel {
        val viewModel = AccessibilitySettingsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun beforeLoading_bothSettingsAreOff() = runTest {
        repository.state.value = AccessibilitySettings(largerText = true, highContrast = true)

        val viewModel = AccessibilitySettingsViewModel(repository)

        assertEquals(AccessibilitySettings(), viewModel.uiState.value)
    }

    @Test
    fun opening_showsTheStoredSettings() = runTest {
        repository.state.value = AccessibilitySettings(largerText = true, highContrast = false)

        assertEquals(AccessibilitySettings(largerText = true, highContrast = false), observedViewModel().uiState.value)
    }

    @Test
    fun repositoryChange_isShownImmediately() = runTest {
        val viewModel = observedViewModel()

        repository.state.value = AccessibilitySettings(largerText = false, highContrast = true)
        advanceUntilIdle()

        assertEquals(AccessibilitySettings(largerText = false, highContrast = true), viewModel.uiState.value)
    }

    @Test
    fun onLargerTextChange_savesItInTheRepository() = runTest {
        val viewModel = observedViewModel()

        viewModel.onLargerTextChange(true)
        advanceUntilIdle()

        assertEquals(listOf(true), repository.largerTextCalls)
        assertEquals(emptyList<Boolean>(), repository.highContrastCalls)
        assertEquals(AccessibilitySettings(largerText = true, highContrast = false), viewModel.uiState.value)
    }

    @Test
    fun onHighContrastChange_savesItInTheRepository() = runTest {
        val viewModel = observedViewModel()

        viewModel.onHighContrastChange(true)
        advanceUntilIdle()

        assertEquals(listOf(true), repository.highContrastCalls)
        assertEquals(emptyList<Boolean>(), repository.largerTextCalls)
        assertEquals(AccessibilitySettings(largerText = false, highContrast = true), viewModel.uiState.value)
    }

    @Test
    fun turningSettingOff_savesFalse() = runTest {
        repository.state.value = AccessibilitySettings(largerText = true, highContrast = true)
        val viewModel = observedViewModel()

        viewModel.onHighContrastChange(false)
        advanceUntilIdle()

        assertEquals(listOf(false), repository.highContrastCalls)
        assertEquals(AccessibilitySettings(largerText = true, highContrast = false), viewModel.uiState.value)
    }
}
