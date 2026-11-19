package com.example.paypulse.dto.terminal;

public record TerminalFilter(TerminalStatus status, TerminalType type) {

    public boolean hasStatus() {
        return status != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
