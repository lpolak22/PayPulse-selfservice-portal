package com.example.paypulse.dto.terminal;

public record TerminalSearchCriteria(String status, String type, Long merchantId) {

    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }

    public boolean hasType() {
        return type != null && !type.isBlank();
    }
}
