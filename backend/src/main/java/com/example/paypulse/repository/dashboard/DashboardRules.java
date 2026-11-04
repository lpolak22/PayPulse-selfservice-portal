package com.example.paypulse.repository.dashboard;

public final class DashboardRules {

    public static final String COUNTED_TRANSACTION = "t.status = 'APPROVED'";
    public static final String DECLINED_TRANSACTION = "t.status = 'DECLINED'";

    private DashboardRules() {
    }
}
