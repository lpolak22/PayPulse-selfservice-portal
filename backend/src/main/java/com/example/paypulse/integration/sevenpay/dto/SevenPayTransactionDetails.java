package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayTransactionDetails(
        String transactionId,
        String transactionType,
        BigDecimal amount,
        String currency,
        String validity,
        String authorizationState,
        String responseCode,
        String authNumber,
        String acquirerHost,
        String acquirerTid,
        String acquirerMid,
        @JsonFormat(pattern = SevenPayTransactionQuery.RESPONSE_TIME_PATTERN) LocalDateTime timestamp,
        String tid,
        String mid,
        String panMasked,
        String cardBrand,
        String rrn,
        String acquirerRrn,
        String paymentType,
        Integer installmentNumber,
        BigDecimal tipAmount
) {
}
