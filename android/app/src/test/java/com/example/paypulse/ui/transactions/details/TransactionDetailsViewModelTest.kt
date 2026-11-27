package com.example.paypulse.ui.transactions.details

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionCard
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.domain.transaction.TransactionDetailsResult
import com.example.paypulse.domain.transaction.TransactionTerminal
import com.example.paypulse.domain.transaction.TransactionType
import com.example.paypulse.ui.transactions.details.StubTransactionDetailsRepository.Companion.details
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
import java.math.BigDecimal

private const val TRANSACTION_ID = 42L

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubTransactionDetailsRepository()

    private fun TestScope.openedViewModel(): TransactionDetailsViewModel =
        TransactionDetailsViewModel(TRANSACTION_ID, repository).also { advanceUntilIdle() }

    private fun TestScope.loadedWith(details: TransactionDetails): TransactionDetailsModel {
        repository.defaultResult = TransactionDetailsResult.Success(details)
        return (openedViewModel().uiState.value as TransactionDetailsUiState.Loaded).details
    }

    @Test
    fun opening_showsLoadingThenTheTransaction() = runTest {
        val viewModel = TransactionDetailsViewModel(TRANSACTION_ID, repository)
        assertEquals(TransactionDetailsUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        assertEquals(TransactionDetailsUiState.Loaded(details().toDetailsModel()), viewModel.uiState.value)
        assertEquals(listOf(TRANSACTION_ID), repository.requestedIds)
    }

    @Test
    fun allFields_areShown() = runTest {
        val expected = TransactionDetailsModel(
            amount = "42,90 €",
            status = TransactionStatus.APPROVED,
            date = "24.10.2026.",
            time = "14:32:07",
            type = TransactionType.SALE,
            authCode = "0A12B3",
            rrn = "629714503218",
            salesChannel = SalesChannel.CONTACTLESS,
            acquirer = "Acquirer A",
            cardBrand = CardBrand.VISA,
            cardNumber = "4111 11•• •••• 4821",
            terminal = DetailsLink(451, listOf("T-00451", "POS")),
            outlet = DetailsLink(12, listOf("PM Ilica 12", "Trgovina Kvadrat d.o.o."))
        )
        assertEquals(expected, loadedWith(details()))
    }

    @Test
    fun transactionOfAnotherOrganisation_showsNotFound() = runTest {
        repository.defaultResult = TransactionDetailsResult.Failure(TransactionDetailsError.NOT_FOUND)
        assertEquals(TransactionDetailsUiState.NotFound, openedViewModel().uiState.value)
    }

    @Test
    fun networkError_showsTheErrorAndRetryLoadsAgain() = runTest {
        repository.enqueue(TransactionDetailsResult.Failure(TransactionDetailsError.NETWORK))
        val viewModel = openedViewModel()
        assertEquals(TransactionDetailsUiState.Error(TransactionDetailsError.NETWORK), viewModel.uiState.value)

        viewModel.onRetry()
        assertEquals(TransactionDetailsUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TransactionDetailsUiState.Loaded)
        assertEquals(listOf(TRANSACTION_ID, TRANSACTION_ID), repository.requestedIds)
    }

    @Test
    fun unknownError_showsTheError() = runTest {
        repository.defaultResult = TransactionDetailsResult.Failure(TransactionDetailsError.UNKNOWN)
        assertEquals(TransactionDetailsUiState.Error(TransactionDetailsError.UNKNOWN), openedViewModel().uiState.value)
    }

    @Test
    fun amount_usesTheTransactionCurrency() = runTest {
        val amount = loadedWith(details().copy(amount = BigDecimal("1234.5"), currency = "USD")).amount
        assertTrue(amount, amount.startsWith("1.234,50"))
        assertTrue(amount, amount.contains("USD") || amount.contains("$"))
        assertFalse(amount, amount.contains("€"))
    }

    @Test
    fun missingOrUnknownCurrency_showsOnlyTheAmountAndCode() = runTest {
        assertEquals("42,90", loadedWith(details().copy(currency = null)).amount)
        assertEquals("42,90 XYZ1", loadedWith(details().copy(currency = "XYZ1")).amount)
    }

    @Test
    fun missingSalesChannelAndCardNumber_leaveOnlyTheBrand() = runTest {
        val noChannelNoPan = details().copy(salesChannel = null, card = TransactionCard(CardBrand.MASTERCARD, null))
        val model = loadedWith(noChannelNoPan)

        assertNull(model.salesChannel)
        assertNull(model.cardNumber)
        assertEquals(CardBrand.MASTERCARD, model.cardBrand)
    }

    @Test
    fun unmaskedCardNumber_isNeverShown() = runTest {
        val unmasked = details().copy(card = TransactionCard(CardBrand.VISA, "4111111111114821"))
        assertNull(loadedWith(unmasked).cardNumber)
    }

    @Test
    fun terminalWithoutType_showsOnlyItsId() = runTest {
        val model = loadedWith(details().copy(terminal = TransactionTerminal(451, "T-00451", null)))
        assertEquals(DetailsLink(451, listOf("T-00451")), model.terminal)
    }
}
