package com.example.paypulse.dto.terminal;

public record TerminalResponse(
        Long id,
        String terminalId,
        String type,
        String status,
        OutletRef outlet,
        MerchantRef merchant
) {

    public record OutletRef(Long id, String name) {
    }

    public record MerchantRef(Long id, String name) {
    }
}
