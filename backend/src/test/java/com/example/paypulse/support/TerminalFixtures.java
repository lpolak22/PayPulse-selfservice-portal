package com.example.paypulse.support;

import org.springframework.jdbc.core.JdbcTemplate;

public final class TerminalFixtures {

    private static final String MCC_CODE = "5411";

    private final JdbcTemplate jdbc;

    public TerminalFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long terminal(long outletId, String terminalId, String type, String status) {
        return jdbc.queryForObject("""
                        insert into terminal (id_outlet, mcc_code, terminal_id, type, status)
                        values (?, ?, ?, ?, ?)
                        returning id_terminal
                        """, Long.class, outletId, MCC_CODE, terminalId, type, status);
    }
}
