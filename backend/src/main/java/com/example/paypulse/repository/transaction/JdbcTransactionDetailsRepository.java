package com.example.paypulse.repository.transaction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcTransactionDetailsRepository implements TransactionDetailsRepository {

    private final JdbcClient jdbcClient;

    public JdbcTransactionDetailsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<TransactionDetailsRow> findInOrganisation(long id, long organisationId) {
        return jdbcClient.sql(TransactionDetailsSql.FIND_IN_ORGANISATION)
                .param("id", id)
                .param("organisationId", organisationId)
                .query((rs, rowNum) -> toRow(rs))
                .optional();
    }

    private static TransactionDetailsRow toRow(ResultSet rs) throws SQLException {
        return new TransactionDetailsRow(
                rs.getLong("id"),
                rs.getObject("transaction_at", OffsetDateTime.class).toInstant(),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getString("status"),
                rs.getString("type"),
                rs.getString("card_brand"),
                rs.getString("masked_pan"),
                rs.getString("sales_channel"),
                rs.getString("auth_code"),
                rs.getString("rrn"),
                rs.getString("acquirer_name"),
                rs.getLong("id_terminal"),
                rs.getString("terminal_id"),
                rs.getString("terminal_type"),
                rs.getLong("id_outlet"),
                rs.getString("outlet_name"),
                rs.getString("outlet_address"),
                rs.getString("outlet_city"),
                rs.getString("outlet_postal_code"),
                rs.getLong("id_merchant"),
                rs.getString("merchant_name")
        );
    }
}
