package com.example.paypulse.ui.merchants.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.ui.theme.PayPulseTheme

private val previewOutlets = listOf(
    Outlet(1, "PM-0001", "PM Ilica 12", "Ilica 12", "Zagreb", "10000", OutletStatus.ACTIVE, 3),
    Outlet(2, "PM-0002", "PM Avenue Mall", "Avenija Dubrovnik 16", "Zagreb", null, OutletStatus.ACTIVE, 3),
    Outlet(3, "PM-0003", "PM Dubrava", "Avenija Dubrava 45", "Zagreb", null, OutletStatus.ACTIVE, 2),
    Outlet(4, "PM-0004", "PM Velika Gorica", "Zagrebačka 8", "Velika Gorica", null, OutletStatus.DEACTIVATED, 1)
)

private val previewTerminals = listOf(
    MerchantTerminal(1, "TK000101", "PM Ilica 12", TerminalStatus.ACTIVE),
    MerchantTerminal(2, "TK000102", "PM Ilica 12", TerminalStatus.SUSPENDED),
    MerchantTerminal(3, "TK000201", "PM Avenue Mall", TerminalStatus.PREPARED)
)

private val previewDetails = MerchantDetails(
    id = 1,
    merchantId = "TR-100245",
    name = "Trgovina Kvadrat d.o.o.",
    oib = "12345678901",
    status = MerchantStatus.ACTIVE,
    outlets = RelatedItems(previewOutlets, total = 4),
    terminals = RelatedItems(previewTerminals, total = 9)
)

private fun loaded(details: MerchantDetails = previewDetails, tab: MerchantDetailsTab = MerchantDetailsTab.OUTLETS) =
    MerchantDetailsUiState(content = MerchantDetailsContent.Loaded(details), selectedTab = tab)

@PreviewLightDark
@Composable
private fun OutletsPreview() = DetailsPreview(loaded())

@PreviewLightDark
@Composable
private fun TerminalsPreview() = DetailsPreview(loaded(tab = MerchantDetailsTab.TERMINALS))

@PreviewLightDark
@Composable
private fun NoOutletsPreview() =
    DetailsPreview(loaded(previewDetails.copy(outlets = RelatedItems(emptyList(), 0))))

@PreviewLightDark
@Composable
private fun NoSearchResultsPreview() = DetailsPreview(loaded().copy(outletSearch = "Split"))

@PreviewLightDark
@Composable
private fun LoadingPreview() = DetailsPreview(MerchantDetailsUiState())

@PreviewLightDark
@Composable
private fun NotFoundPreview() =
    DetailsPreview(MerchantDetailsUiState(content = MerchantDetailsContent.Error(MerchantDetailsError.NOT_FOUND)))

@Composable
private fun DetailsPreview(state: MerchantDetailsUiState) {
    PayPulseTheme {
        MerchantDetailsScreen(state = state, onEvent = {})
    }
}
