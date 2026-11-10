package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayMerchant(
        String shortName,
        String fullName,
        String oib,
        String status,
        SevenPayAddress address
) {
}
