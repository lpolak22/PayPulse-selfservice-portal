package com.example.paypulse.dto.merchant;

import java.util.Arrays;

public enum MerchantStatus {
    ACTIVE,
    DEACTIVATED,
    HOLD;

    public static final String MESSAGE = "must be ACTIVE, DEACTIVATED or HOLD";

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(status -> status.name().equalsIgnoreCase(value));
    }
}
