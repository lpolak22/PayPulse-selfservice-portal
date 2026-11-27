package com.example.paypulse.dto.terminal;

import java.util.Arrays;
import java.util.Optional;

public enum TerminalStatus {
    PREPARED,
    AUTHENTICATED,
    ACTIVE,
    INACTIVE,
    DEACTIVATED,
    SUSPENDED,
    BLOCKED,
    UNKNOWN;

    public static final String MESSAGE =
            "must be PREPARED, AUTHENTICATED, ACTIVE, INACTIVE, DEACTIVATED, SUSPENDED, BLOCKED or UNKNOWN";

    public static Optional<TerminalStatus> fromParam(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.strip()))
                .findFirst();
    }
}
