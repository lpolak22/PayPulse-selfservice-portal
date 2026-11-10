package com.example.paypulse.repository.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRow(
        long id,
        Instant transactionAt,
        BigDecimal amount,
        String currency,
        String status,
        String type,
        String cardBrand,
        String maskedPan,
        String salesChannel,
        String outletName,
        String terminalId,
        String authCode,
        String rrn
) {
}
