package com.example.paypulse.integration.sevenpay.sync;

public enum SyncSkipReason {
    MISSING_DATA,
    UNSUPPORTED_TYPE,
    UNSUPPORTED_CURRENCY,
    DUMMY_CARD_BRAND,
    UNKNOWN_CARD_BRAND,
    UNKNOWN_TERMINAL
}
