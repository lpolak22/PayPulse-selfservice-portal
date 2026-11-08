package com.example.paypulse.integration.sevenpay.importer;

public final class SevenPayStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";

    private SevenPayStatus() {
    }

    public static String toLocal(String status) {
        return "active".equalsIgnoreCase(status) ? ACTIVE : INACTIVE;
    }
}
