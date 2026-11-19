package com.example.paypulse.dto.terminal;

import java.util.Arrays;
import java.util.Optional;

public enum TerminalSortField {
    TID("terminalId"),
    TYPE("type"),
    STATUS("status"),
    OUTLET("outlet.name"),
    MERCHANT("outlet.merchant.name");

    private final String property;

    TerminalSortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static Optional<TerminalSortField> fromParam(String value) {
        return Arrays.stream(values())
                .filter(field -> field.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
