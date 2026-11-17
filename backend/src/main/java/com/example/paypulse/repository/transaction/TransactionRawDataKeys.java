package com.example.paypulse.repository.transaction;

public final class TransactionRawDataKeys {

    public static final String TYPE = "type";
    public static final String CARD_BRAND = "card_brand";
    public static final String MASKED_PAN = "masked_pan";
    public static final String SALES_CHANNEL = "sales_channel";
    public static final String AUTH_CODE = "auth_code";
    public static final String RRN = "rrn";

    private TransactionRawDataKeys() {
    }

    public static String text(String key) {
        return "t.raw_data ->> '" + key + "'";
    }
}
