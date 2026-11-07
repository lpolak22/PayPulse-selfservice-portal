package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPaySalepoint(
        String mid,
        String name,
        String merchantOib,
        String status,
        SevenPayAddress address
) {
}
