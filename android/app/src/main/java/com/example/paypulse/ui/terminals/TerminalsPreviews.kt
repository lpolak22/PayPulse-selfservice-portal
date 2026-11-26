package com.example.paypulse.ui.terminals

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType
import com.example.paypulse.ui.theme.PayPulseTheme

private val previewTerminals = listOf(
    Terminal(1, "T-00451", "SoftPOS V1", TerminalStatus.ACTIVE, "ACTIVE", "PM Ilica 12", "Trgovina Kvadrat d.o.o."),
    Terminal(2, "T-00452", "POS", TerminalStatus.INACTIVE, "INACTIVE", "PM Ilica 12", "Trgovina Kvadrat d.o.o."),
    Terminal(3, "T-00460", "ECR", TerminalStatus.DEACTIVATED, "DEACTIVATED", "Kavana Jelačić", "Zagreb Retail Group d.o.o."),
    Terminal(4, "T-00471", "MOBILE", TerminalStatus.SUSPENDED, "SUSPENDED", "Pekara Kruh i Sol", "Zagreb Retail Group d.o.o."),
    Terminal(5, "T-00480", "SoftPOS V2", TerminalStatus.PREPARED, "PREPARED", "PM Savska 5", "Trgovina Kvadrat d.o.o."),
    Terminal(6, "T-00499", "Kiosk X", null, "RETIRED", "PM Vukovarska 1", "Trgovina Kvadrat d.o.o.")
)

private fun loadedState(
    paging: TerminalsPaging = TerminalsPaging.IDLE,
    status: TerminalStatus? = null,
    type: TerminalType? = null
) = TerminalsUiState(
    status = status,
    type = type,
    content = TerminalsContent.Loaded(terminals = previewTerminals, total = 42, paging = paging)
)

@PreviewLightDark
@Composable
private fun TerminalsFilledPreview() = TerminalsPreview(loadedState())

@PreviewLightDark
@Composable
private fun TerminalsFilteredPreview() =
    TerminalsPreview(loadedState(status = TerminalStatus.ACTIVE, type = TerminalType.SOFTPOS_V1))

@PreviewLightDark
@Composable
private fun TerminalsLoadMoreErrorPreview() = TerminalsPreview(loadedState(paging = TerminalsPaging.APPEND_ERROR))

@PreviewLightDark
@Composable
private fun TerminalsLoadingPreview() = TerminalsPreview(TerminalsUiState())

@PreviewLightDark
@Composable
private fun TerminalsEmptyFilteredPreview() =
    TerminalsPreview(TerminalsUiState(status = TerminalStatus.BLOCKED, content = TerminalsContent.Empty))

@PreviewLightDark
@Composable
private fun TerminalsNoTerminalsPreview() = TerminalsPreview(TerminalsUiState(content = TerminalsContent.Empty))

@PreviewLightDark
@Composable
private fun TerminalsErrorPreview() =
    TerminalsPreview(TerminalsUiState(content = TerminalsContent.Error(TerminalError.NETWORK)))

@Composable
private fun TerminalsPreview(state: TerminalsUiState) {
    PayPulseTheme {
        TerminalsScreen(state = state, onEvent = {})
    }
}
