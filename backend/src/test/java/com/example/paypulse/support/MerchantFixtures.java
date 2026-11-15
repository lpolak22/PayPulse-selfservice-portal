package com.example.paypulse.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class MerchantFixtures {

    private static final String MCC_CODE = "5411";

    private final JdbcTemplate jdbc;

    public MerchantFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long merchant(long organisationId, String name, String oib, String sourceId, String status) {
        return jdbc.queryForObject("""
                        insert into merchant (id_organisation, source_id, name, registration_number, status)
                        values (?, ?, ?, ?, ?)
                        returning id_merchant
                        """, Long.class, organisationId, sourceId, name, oib, status);
    }

    public long merchant(long organisationId, String name) {
        return merchant(organisationId, name, oib(), sourceId(), "ACTIVE");
    }

    public long outlet(long merchantId) {
        return jdbc.queryForObject("""
                        insert into outlet (id_merchant, name, status)
                        values (?, 'Test outlet', 'ACTIVE')
                        returning id_outlet
                        """, Long.class, merchantId);
    }

    public void terminal(long outletId) {
        jdbc.update("insert into mcc_code (mcc_code, description) values (?, 'Test') on conflict do nothing", MCC_CODE);
        jdbc.update("""
                insert into terminal (id_outlet, mcc_code, terminal_id, type, status)
                values (?, ?, ?, 'SoftPOS V1', 'ACTIVE')
                """, outletId, MCC_CODE, "T" + unique(15));
    }

    public static String oib() {
        return oibStartingWith("0");
    }

    public static String oibStartingWith(String prefix) {
        StringBuilder oib = new StringBuilder(prefix);
        while (oib.length() < 11) {
            oib.append(ThreadLocalRandom.current().nextInt(10));
        }
        return oib.toString();
    }

    public static String sourceId() {
        return "SP-" + unique(12).toUpperCase();
    }

    private static String unique(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
