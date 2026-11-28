package com.example.paypulse.ui.outlets

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.ui.theme.PayPulseTheme

private val previewOutlets = listOf(
    Outlet(1, "MID-ZG-001", "Konzum Ilica", "Ilica 1", "Zagreb", "10000", OutletStatus.ACTIVE, 4),
    Outlet(2, "MID-ZG-002", "Konzum Dubrava", "Avenija Dubrava 45", "Zagreb", "10040", OutletStatus.HOLD, 2),
    Outlet(3, "MID-RI-001", "Konzum Korzo", "Korzo 12", "Rijeka", "51000", OutletStatus.ACTIVE, 3),
    Outlet(4, "MID-ST-001", "Konzum Riva", null, "Split", null, OutletStatus.DEACTIVATED, 0),
    Outlet(5, null, "Konzum Pula", "Giardini 2", null, "52100", null, 1)
)

private val previewCities = listOf("Pula", "Rijeka", "Split", "Zagreb")

private fun loadedState(paging: OutletsPaging = OutletsPaging.IDLE, city: String? = null) = OutletsUiState(
    merchantName = "Konzum d.d.",
    city = city,
    cities = previewCities,
    content = OutletsContent.Loaded(outlets = previewOutlets, total = 27, paging = paging)
)

@PreviewLightDark
@Composable
private fun OutletsFilledPreview() = OutletsPreview(loadedState())

@PreviewLightDark
@Composable
private fun OutletsCityFilteredPreview() = OutletsPreview(loadedState(city = "Rijeka"))

@PreviewLightDark
@Composable
private fun OutletsLoadMoreErrorPreview() = OutletsPreview(loadedState(paging = OutletsPaging.APPEND_ERROR))

@PreviewLightDark
@Composable
private fun OutletsLoadingPreview() = OutletsPreview(OutletsUiState(merchantName = "Konzum d.d."))

@PreviewLightDark
@Composable
private fun OutletsNoMatchPreview() = OutletsPreview(
    OutletsUiState(merchantName = "Konzum d.d.", city = "Rijeka", cities = previewCities, content = OutletsContent.Empty)
)

@PreviewLightDark
@Composable
private fun OutletsNoOutletsPreview() =
    OutletsPreview(OutletsUiState(merchantName = "Pekara Kruh i Sol", content = OutletsContent.Empty))

@PreviewLightDark
@Composable
private fun OutletsErrorPreview() = OutletsPreview(
    OutletsUiState(merchantName = "Konzum d.d.", content = OutletsContent.Error(OutletError.NETWORK))
)

@PreviewLightDark
@Composable
private fun OutletsNotFoundPreview() =
    OutletsPreview(OutletsUiState(content = OutletsContent.Error(OutletError.MERCHANT_NOT_FOUND)))

@Composable
private fun OutletsPreview(state: OutletsUiState) {
    PayPulseTheme {
        OutletsScreen(state = state, onEvent = {})
    }
}
