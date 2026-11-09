package com.example.paypulse.integration.sevenpay.importer;

import java.util.Locale;

public final class SevenPayStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String UNKNOWN = "UNKNOWN";

    private SevenPayStatus() {
    }

    public static String toLocal(String status) {
        return status == null || status.isBlank() ? UNKNOWN : status.strip().toUpperCase(Locale.ROOT);
    }
}
