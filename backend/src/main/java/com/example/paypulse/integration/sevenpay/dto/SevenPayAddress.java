package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayAddress(String country, String city, String streetAddressNumber, String zipCode) {
}
