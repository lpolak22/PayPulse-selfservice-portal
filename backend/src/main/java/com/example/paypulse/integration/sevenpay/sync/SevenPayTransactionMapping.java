package com.example.paypulse.integration.sevenpay.sync;

import java.math.BigDecimal;
import java.util.Map;

public sealed interface SevenPayTransactionMapping {

    record Mapped(
            String sourceId,
            String tid,
            String acquirerCode,
            BigDecimal amount,
            String currency,
            String status,
            Map<String, Object> rawData
    ) implements SevenPayTransactionMapping {
    }

    record Skipped(String sourceId, SyncSkipReason reason) implements SevenPayTransactionMapping {
    }
}
