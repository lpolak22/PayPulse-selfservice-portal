package com.example.paypulse.repository.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public record LatestTransactionRow(
        String cardBrand,
        String maskedPan,
        String outletName,
        Instant transactionAt,
        BigDecimal amount,
        String status
) {
}
