package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayTerminal(
        String tid,
        String terminalType,
        String status,
        String midReference,
        String merchantCategoryCode,
        String currency,
        List<SevenPayAcquirerTerminal> acquirerTerminalData
) {
}
