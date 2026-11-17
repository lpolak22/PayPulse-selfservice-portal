package com.example.paypulse.repository.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDetailsRow(
        long id,
        Instant transactionAt,
        BigDecimal amount,
        String currency,
        String status,
        String type,
        String cardBrand,
        String maskedPan,
        String salesChannel,
        String authCode,
        String rrn,
        String acquirerName,
        long idTerminal,
        String terminalId,
        String terminalType,
        long idOutlet,
        String outletName,
        String outletAddress,
        String outletCity,
        String outletPostalCode,
        long idMerchant,
        String merchantName
) {
}
