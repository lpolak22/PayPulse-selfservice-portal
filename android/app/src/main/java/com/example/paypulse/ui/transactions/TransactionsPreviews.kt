package com.example.paypulse.ui.transactions

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.Transaction
import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.domain.transaction.TransactionFilters
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.TransactionType
import com.example.paypulse.ui.theme.PayPulseTheme
import com.example.paypulse.ui.transactions.filters.TransactionFilterSheetContent
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

private val previewToday = LocalDate.of(2026, 11, 11)
private val previewDefaultRange = DateRange(previewToday.minusDays(6), previewToday)
private val previewFilters = TransactionFilters(
    dateRange = previewDefaultRange,
    cardBrands = setOf(CardBrand.VISA),
    statuses = setOf(TransactionStatus.APPROVED)
)

private val previewTransactions = listOf(
    previewTransaction(1, "2026-11-11T12:41:00Z", "1234.56", TransactionStatus.APPROVED, CardBrand.VISA, SalesChannel.POS),
    previewTransaction(2, "2026-11-11T09:05:00Z", "18.90", TransactionStatus.DECLINED, CardBrand.MASTERCARD, SalesChannel.CONTACTLESS),
    previewTransaction(3, "2026-11-10T17:22:00Z", "245.00", TransactionStatus.REFUNDED, CardBrand.MAESTRO, SalesChannel.ECOM),
    previewTransaction(4, "2026-11-08T08:10:00Z", "72.40", null, CardBrand.OTHER, null)
)

private fun previewTransaction(
    id: Long,
    at: String,
    amount: String,
    status: TransactionStatus?,
    brand: CardBrand,
    channel: SalesChannel?
): Transaction = Transaction(
    id = id,
    transactionAt = Instant.parse(at),
    amount = BigDecimal(amount),
    currency = "EUR",
    status = status,
    type = TransactionType.SALE,
    cardBrand = brand,
    maskedPan = "411111******10$id$id",
    salesChannel = channel,
    outletName = "Zagreb Centar",
    terminalId = "T00012$id",
    authCode = "A1B2C$id",
    rrn = "61531234567$id"
)

private fun loadedState(sort: TransactionSort = TransactionSort.NEWEST_FIRST, paging: PagingStatus = PagingStatus.IDLE) =
    TransactionsUiState(
        filters = previewFilters,
        sort = sort,
        activeFilters = previewFilters.activeFilters(previewDefaultRange),
        content = TransactionsContent.Loaded(
            items = previewTransactions.toListItems(sort, previewToday),
            total = 128,
            paging = paging
        )
    )

private fun stateWith(content: TransactionsContent) =
    TransactionsUiState(filters = TransactionFilters(previewDefaultRange), content = content)

@PreviewLightDark
@Composable
private fun TransactionsFilledPreview() = TransactionsPreview(loadedState())

@PreviewLightDark
@Composable
private fun TransactionsByAmountPreview() = TransactionsPreview(loadedState(sort = TransactionSort.HIGHEST_AMOUNT_FIRST))

@PreviewLightDark
@Composable
private fun TransactionsLoadMoreErrorPreview() = TransactionsPreview(loadedState(paging = PagingStatus.APPEND_ERROR))

@PreviewLightDark
@Composable
private fun TransactionsLoadingPreview() = TransactionsPreview(stateWith(TransactionsContent.Loading))

@PreviewLightDark
@Composable
private fun TransactionsEmptyPreview() = TransactionsPreview(stateWith(TransactionsContent.Empty))

@PreviewLightDark
@Composable
private fun TransactionsErrorPreview() =
    TransactionsPreview(stateWith(TransactionsContent.Error(TransactionError.NETWORK)))

@PreviewLightDark
@Composable
private fun TransactionsInvalidFilterPreview() =
    TransactionsPreview(stateWith(TransactionsContent.Error(TransactionError.INVALID_FILTER)))

@Preview(name = "Filters open", heightDp = 1300)
@Composable
private fun TransactionsFiltersOpenPreview() {
    PayPulseTheme {
        Surface {
            TransactionFilterSheetContent(
                draft = FilterDraft(previewFilters.copy(authCode = "A1B2C3"), TransactionSort.NEWEST_FIRST, previewToday),
                onEvent = {}
            )
        }
    }
}

@Composable
private fun TransactionsPreview(state: TransactionsUiState) {
    PayPulseTheme {
        TransactionsScreen(state = state, onEvent = {})
    }
}
