package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayAcquirerTerminal(String acquirerTid, String acquirerHost) {
}
