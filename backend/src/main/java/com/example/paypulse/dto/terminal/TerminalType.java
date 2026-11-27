package com.example.paypulse.dto.terminal;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum TerminalType {
    SOFTPOS_V1("SoftPOS V1"),
    SOFTPOS_V2("SoftPOS V2"),
    POS("POS"),
    MOBILE("MOBILE"),
    ECR("ECR"),
    VIRTUAL("VIRTUAL");

    public static final String MESSAGE = "must be SoftPOS V1, SoftPOS V2, POS, MOBILE, ECR or VIRTUAL";

    private final String label;

    TerminalType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public String key() {
        return normalise(label);
    }

    public static Optional<TerminalType> fromParam(String value) {
        String key = normalise(value);
        return Arrays.stream(values())
                .filter(type -> type.key().equals(key) || normalise(type.name()).equals(key))
                .findFirst();
    }

    public static String normalise(String value) {
        return value.replace(" ", "").replace("_", "").toLowerCase(Locale.ROOT);
    }
}
