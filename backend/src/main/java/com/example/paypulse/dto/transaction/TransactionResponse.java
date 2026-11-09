package com.example.paypulse.dto.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
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
