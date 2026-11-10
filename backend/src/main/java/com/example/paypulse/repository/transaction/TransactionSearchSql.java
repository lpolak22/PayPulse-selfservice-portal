package com.example.paypulse.repository.transaction;

import org.springframework.data.domain.Sort;

final class TransactionSearchSql {

    private static final String SELECT_ITEMS = """
            SELECT t.id_transaction AS id,
                   t.transaction_at AS transaction_at,
                   t.amount AS amount,
                   t.currency AS currency,
                   t.status AS status,
                   t.raw_data ->> 'type' AS type,
                   t.raw_data ->> 'card_brand' AS card_brand,
                   t.raw_data ->> 'masked_pan' AS masked_pan,
                   t.raw_data ->> 'sales_channel' AS sales_channel,
                   o.name AS outlet_name,
                   te.terminal_id AS terminal_id,
                   t.raw_data ->> 'auth_code' AS auth_code,
                   t.raw_data ->> 'rrn' AS rrn
            """;

    private static final String SELECT_COUNT = "SELECT COUNT(*)";

    private static final String ORGANISATION_TRANSACTIONS_IN_RANGE = """
             FROM transaction t
             JOIN terminal te ON te.id_terminal = t.id_terminal
             JOIN outlet o ON o.id_outlet = te.id_outlet
             JOIN merchant m ON m.id_merchant = o.id_merchant
             WHERE m.id_organisation = :organisationId
               AND t.transaction_at >= :start
               AND t.transaction_at < :end
            """;

    private TransactionSearchSql() {
    }

    static String page(TransactionWhereClause where, TransactionSortField field, Sort.Direction direction) {
        String order = " " + direction.name();
        return SELECT_ITEMS + ORGANISATION_TRANSACTIONS_IN_RANGE + where.sql()
                + " ORDER BY " + field.column() + order + ", t.id_transaction" + order
                + " LIMIT :limit OFFSET :offset";
    }

    static String count(TransactionWhereClause where) {
        return SELECT_COUNT + ORGANISATION_TRANSACTIONS_IN_RANGE + where.sql();
    }
}
