package com.example.paypulse.ui.transactions.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionCard
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.domain.transaction.TransactionMerchant
import com.example.paypulse.domain.transaction.TransactionOutlet
import com.example.paypulse.domain.transaction.TransactionTerminal
import com.example.paypulse.domain.transaction.TransactionType
import com.example.paypulse.ui.theme.PayPulseTheme
import java.math.BigDecimal
import java.time.Instant

private val previewDetails = TransactionDetails(
    id = 42,
    transactionAt = Instant.parse("2026-10-24T12:32:07Z"),
    amount = BigDecimal("42.90"),
    currency = "EUR",
    status = TransactionStatus.APPROVED,
    type = TransactionType.SALE,
    salesChannel = SalesChannel.CONTACTLESS,
    authCode = "0A12B3",
    rrn = "629714503218",
    acquirer = "Acquirer A",
    card = TransactionCard(CardBrand.VISA, "411111******4821"),
    terminal = TransactionTerminal(451, "T-00451", "POS"),
    outlet = TransactionOutlet(12, "PM Ilica 12", "Ilica 12", "Zagreb", "10000"),
    merchant = TransactionMerchant(3, "Trgovina Kvadrat d.o.o.")
)

private val previewSparseDetails = previewDetails.copy(
    amount = BigDecimal("1250.00"),
    currency = "USD",
    status = TransactionStatus.DECLINED,
    salesChannel = null,
    rrn = null,
    card = TransactionCard(CardBrand.MASTERCARD, null)
)

private val previewActions = TransactionDetailsActions(onBackClick = {}, onRetry = {}, onTerminalClick = {}, onOutletClick = {})

@PreviewLightDark
@Composable
private fun TransactionDetailsApprovedPreview() =
    TransactionDetailsPreview(TransactionDetailsUiState.Loaded(previewDetails.toDetailsModel()))

@PreviewLightDark
@Composable
private fun TransactionDetailsDeclinedWithoutOptionalFieldsPreview() =
    TransactionDetailsPreview(TransactionDetailsUiState.Loaded(previewSparseDetails.toDetailsModel()))

@PreviewLightDark
@Composable
private fun TransactionDetailsRefundedPreview() = TransactionDetailsPreview(
    TransactionDetailsUiState.Loaded(
        previewDetails.copy(status = TransactionStatus.REFUNDED, type = TransactionType.REFUND).toDetailsModel()
    )
)

@PreviewLightDark
@Composable
private fun TransactionDetailsCancelledPreview() = TransactionDetailsPreview(
    TransactionDetailsUiState.Loaded(previewDetails.copy(status = TransactionStatus.CANCELLED).toDetailsModel())
)

@PreviewLightDark
@Composable
private fun TransactionDetailsLoadingPreview() = TransactionDetailsPreview(TransactionDetailsUiState.Loading)

@PreviewLightDark
@Composable
private fun TransactionDetailsNotFoundPreview() = TransactionDetailsPreview(TransactionDetailsUiState.NotFound)

@PreviewLightDark
@Composable
private fun TransactionDetailsNetworkErrorPreview() =
    TransactionDetailsPreview(TransactionDetailsUiState.Error(TransactionDetailsError.NETWORK))

@Composable
private fun TransactionDetailsPreview(state: TransactionDetailsUiState) {
    PayPulseTheme {
        TransactionDetailsScreen(state = state, actions = previewActions)
    }
}
