package com.example.paypulse.dto.transaction;

public record TransactionCardResponse(
        String brand,
        String maskedPan
) {
}
