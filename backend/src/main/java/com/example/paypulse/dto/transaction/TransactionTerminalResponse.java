package com.example.paypulse.dto.transaction;

public record TransactionTerminalResponse(
        long id,
        String terminalId,
        String type
) {
}
