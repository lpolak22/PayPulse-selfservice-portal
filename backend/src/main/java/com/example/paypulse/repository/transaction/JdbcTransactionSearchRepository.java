package com.example.paypulse.repository.transaction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class JdbcTransactionSearchRepository implements TransactionSearchRepository {

    private final JdbcClient jdbcClient;

    public JdbcTransactionSearchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<TransactionRow> findPage(TransactionSearchQuery query) {
        TransactionWhereClause where = TransactionWhereClause.of(query);
        return jdbcClient.sql(TransactionSearchSql.page(where, query.sortField(), query.direction()))
                .params(where.params())
                .param("limit", query.limit())
                .param("offset", query.offset())
                .query((rs, rowNum) -> toRow(rs))
                .list();
    }

    @Override
    public long count(TransactionSearchQuery query) {
        TransactionWhereClause where = TransactionWhereClause.of(query);
        return jdbcClient.sql(TransactionSearchSql.count(where))
                .params(where.params())
                .query(Long.class)
                .single();
    }

    private static TransactionRow toRow(ResultSet rs) throws SQLException {
        return new TransactionRow(
                rs.getLong("id"),
                rs.getObject("transaction_at", OffsetDateTime.class).toInstant(),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getString("status"),
                rs.getString("type"),
                rs.getString("card_brand"),
                rs.getString("masked_pan"),
                rs.getString("sales_channel"),
                rs.getString("outlet_name"),
                rs.getString("terminal_id"),
                rs.getString("auth_code"),
                rs.getString("rrn")
        );
    }
}
