package com.example.paypulse.dto.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDetailsResponse(
        long id,
        Instant transactionAt,
        BigDecimal amount,
        String currency,
        String status,
        String type,
        String salesChannel,
        String authCode,
        String rrn,
        String acquirer,
        TransactionCardResponse card,
        TransactionTerminalResponse terminal,
        TransactionOutletResponse outlet,
        TransactionMerchantResponse merchant
) {
}
