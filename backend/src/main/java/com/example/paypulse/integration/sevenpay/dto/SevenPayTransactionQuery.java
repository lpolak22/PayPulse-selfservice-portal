package com.example.paypulse.integration.sevenpay.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public record SevenPayTransactionQuery(String merchantOib, LocalDateTime timeFrom, LocalDateTime timeUntil) {

    public static final String REQUEST_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RESPONSE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss[.SSS]";

    public SevenPayTransactionQuery {
        Objects.requireNonNull(merchantOib, "merchantOib");
    }
}
