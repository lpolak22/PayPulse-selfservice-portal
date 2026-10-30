package com.example.paypulse.seed.generator;

import java.math.BigDecimal;
import java.util.Map;

public record SeedTransaction(
        long terminalId,
        long acquirerId,
        String sourceId,
        BigDecimal amount,
        String currency,
        String status,
        Map<String, Object> rawData
) {
}
