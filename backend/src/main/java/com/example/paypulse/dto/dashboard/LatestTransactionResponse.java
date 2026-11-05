package com.example.paypulse.dto.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public record LatestTransactionResponse(
        String cardBrand,
        String maskedPan,
        String outletName,
        Instant transactionAt,
        BigDecimal amount,
        String status
) {
}
