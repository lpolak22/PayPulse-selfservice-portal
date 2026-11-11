package com.example.paypulse.repository.dashboard;

public final class DashboardRules {

    public static final String EUR_TRANSACTION = "t.currency = 'EUR'";
    public static final String COUNTED_TRANSACTION = "t.status = 'APPROVED'"
            + " AND t.raw_data ->> 'type' IN ('SALE', 'SALE_WITH_INSTALLMENTS')"
            + " AND " + EUR_TRANSACTION;
    public static final String DECLINED_TRANSACTION = "t.status = 'DECLINED' AND " + EUR_TRANSACTION;

    private DashboardRules() {
    }
}
