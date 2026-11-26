package com.example.paypulse.ui.terminals

import androidx.lifecycle.SavedStateHandle
import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalResult
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType
import com.example.paypulse.ui.terminals.StubTerminalRepository.Companion.successPage
import com.example.paypulse.ui.terminals.StubTerminalRepository.Companion.terminals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubTerminalRepository()
    private val savedState = SavedStateHandle()

    private fun TestScope.openedViewModel(): TerminalsViewModel =
        TerminalsViewModel(repository, savedState).also { advanceUntilIdle() }

    private fun TerminalsViewModel.loaded(): TerminalsContent.Loaded =
        uiState.value.content as TerminalsContent.Loaded

    @Test
    fun opening_showsLoadingThenTheFirstPage() = runTest {
        val viewModel = TerminalsViewModel(repository, savedState)
        assertEquals(TerminalsContent.Loading, viewModel.uiState.value.content)
        advanceUntilIdle()

        assertEquals(terminals(1..2), viewModel.loaded().terminals)
        assertEquals(2L, viewModel.loaded().total)
        assertEquals(TerminalsPaging.END, viewModel.loaded().paging)
        assertEquals(0, repository.queries.single().page)
    }

    @Test
    fun noTerminals_showsEmpty() = runTest {
        repository.defaultResult = successPage(emptyList(), total = 0)
        assertEquals(TerminalsContent.Empty, openedViewModel().uiState.value.content)
    }

    @Test
    fun failedFirstPage_showsErrorAndRetryReloads() = runTest {
        repository.enqueue(TerminalResult.Failure(TerminalError.NETWORK))
        val viewModel = openedViewModel()
        assertEquals(TerminalsContent.Error(TerminalError.NETWORK), viewModel.uiState.value.content)

        viewModel.onEvent(TerminalsEvent.Retry)
        advanceUntilIdle()
        assertEquals(terminals(1..2), viewModel.loaded().terminals)
        assertEquals(0, repository.queries.last().page)
    }

    @Test
    fun selectingAStatus_reloadsFromPageZero() = runTest {
        repository.enqueue(successPage(terminals(1..20), total = 25), successPage(terminals(21..25), total = 25, page = 1))
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.LoadMore)
        advanceUntilIdle()

        viewModel.onEvent(TerminalsEvent.StatusSelected(TerminalStatus.BLOCKED))
        assertEquals(TerminalsContent.Loading, viewModel.uiState.value.content)
        advanceUntilIdle()

        assertEquals(TerminalStatus.BLOCKED, repository.queries.last().status)
        assertEquals(0, repository.queries.last().page)
        assertEquals(terminals(1..2), viewModel.loaded().terminals)
    }

    @Test
    fun selectingAType_reloadsFromPageZeroKeepingStatus() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.StatusSelected(TerminalStatus.ACTIVE))
        viewModel.onEvent(TerminalsEvent.TypeSelected(TerminalType.SOFTPOS_V1))
        advanceUntilIdle()

        val query = repository.queries.last()
        assertEquals(TerminalStatus.ACTIVE, query.status)
        assertEquals(TerminalType.SOFTPOS_V1, query.type)
        assertEquals(0, query.page)
    }

    @Test
    fun clearFilters_resetsStatusAndType() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.StatusSelected(TerminalStatus.ACTIVE))
        viewModel.onEvent(TerminalsEvent.TypeSelected(TerminalType.POS))
        viewModel.onEvent(TerminalsEvent.ClearFilters)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasFilters)
        assertNull(repository.queries.last().status)
        assertNull(repository.queries.last().type)
    }

    @Test
    fun loadMore_appendsTheNextPage() = runTest {
        repository.enqueue(successPage(terminals(1..20), total = 25), successPage(terminals(21..25), total = 25, page = 1))
        val viewModel = openedViewModel()
        assertEquals(TerminalsPaging.IDLE, viewModel.loaded().paging)

        viewModel.onEvent(TerminalsEvent.LoadMore)
        advanceUntilIdle()

        assertEquals(terminals(1..25), viewModel.loaded().terminals)
        assertEquals(TerminalsPaging.END, viewModel.loaded().paging)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun loadMoreAtEnd_doesNothing() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.LoadMore)
        advanceUntilIdle()

        assertEquals(1, repository.queries.size)
        assertEquals(TerminalsPaging.END, viewModel.loaded().paging)
    }

    @Test
    fun failedNextPage_keepsTheListAndRetryLoadsItAgain() = runTest {
        repository.enqueue(
            successPage(terminals(1..20), total = 25),
            TerminalResult.Failure(TerminalError.NETWORK),
            successPage(terminals(21..25), total = 25, page = 1)
        )
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(TerminalsPaging.APPEND_ERROR, viewModel.loaded().paging)
        assertEquals(terminals(1..20), viewModel.loaded().terminals)

        viewModel.onEvent(TerminalsEvent.Retry)
        advanceUntilIdle()
        assertEquals(terminals(1..25), viewModel.loaded().terminals)
        assertEquals(1, repository.queries.last().page)
    }

    @Test
    fun refresh_keepsTheListVisibleAndReplacesIt() = runTest {
        val viewModel = openedViewModel()
        repository.defaultResult = successPage(terminals(3..3), total = 1)

        viewModel.onEvent(TerminalsEvent.Refresh)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(terminals(1..2), viewModel.loaded().terminals)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(terminals(3..3), viewModel.loaded().terminals)
        assertEquals(0, repository.queries.last().page)
    }

    @Test
    fun filters_areRestoredFromSavedState() = runTest {
        val viewModel = openedViewModel()
        viewModel.onEvent(TerminalsEvent.StatusSelected(TerminalStatus.SUSPENDED))
        viewModel.onEvent(TerminalsEvent.TypeSelected(TerminalType.ECR))
        advanceUntilIdle()

        val restored = openedViewModel()

        assertEquals(TerminalStatus.SUSPENDED, restored.uiState.value.status)
        assertEquals(TerminalType.ECR, restored.uiState.value.type)
        assertEquals(TerminalStatus.SUSPENDED, repository.queries.last().status)
        assertEquals(TerminalType.ECR, repository.queries.last().type)
    }
}
