package com.example.paypulse.ui.merchants

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.ui.theme.PayPulseTheme

private val previewMerchants = listOf(
    Merchant(id = 1, merchantId = "7PAYMENTS", name = "7 PAYMENTS d.o.o", oib = "34855856319", status = MerchantStatus.ACTIVE, outletCount = 26, terminalCount = 40),
    Merchant(id = 2, merchantId = "7PAY-SUB", name = "7Pay podtrgovac", oib = "59894558130", status = MerchantStatus.HOLD, outletCount = 1, terminalCount = 2),
    Merchant(id = 3, merchantId = "ZRG-M1", name = "Zagreb Retail Group d.o.o.", oib = "12345678903", status = MerchantStatus.ACTIVE, outletCount = 4, terminalCount = 9),
    Merchant(id = 4, merchantId = "ZRG-M2", name = "Kavana Jelačić", oib = "98765432106", status = MerchantStatus.DEACTIVATED, outletCount = 2, terminalCount = 3),
    Merchant(id = 5, merchantId = null, name = "Pekara Kruh i Sol", oib = "45678912307", status = null, outletCount = 0, terminalCount = 0)
)

private fun loadedState(paging: MerchantsPaging = MerchantsPaging.IDLE, status: MerchantStatus? = null) =
    MerchantsUiState(
        status = status,
        content = MerchantsContent.Loaded(merchants = previewMerchants, total = 42, paging = paging)
    )

@PreviewLightDark
@Composable
private fun MerchantsFilledPreview() = MerchantsPreview(loadedState())

@PreviewLightDark
@Composable
private fun MerchantsFilteredPreview() = MerchantsPreview(loadedState(status = MerchantStatus.ACTIVE))

@PreviewLightDark
@Composable
private fun MerchantsLoadMoreErrorPreview() = MerchantsPreview(loadedState(paging = MerchantsPaging.APPEND_ERROR))

@PreviewLightDark
@Composable
private fun MerchantsLoadingPreview() = MerchantsPreview(MerchantsUiState())

@PreviewLightDark
@Composable
private fun MerchantsEmptyPreview() =
    MerchantsPreview(MerchantsUiState(searchText = "xyz", content = MerchantsContent.Empty))

@PreviewLightDark
@Composable
private fun MerchantsNoMerchantsPreview() = MerchantsPreview(MerchantsUiState(content = MerchantsContent.Empty))

@PreviewLightDark
@Composable
private fun MerchantsErrorPreview() =
    MerchantsPreview(MerchantsUiState(content = MerchantsContent.Error(MerchantError.NETWORK)))

@Composable
private fun MerchantsPreview(state: MerchantsUiState) {
    PayPulseTheme {
        MerchantsScreen(state = state, onEvent = {})
    }
}
