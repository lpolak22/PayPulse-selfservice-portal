package com.example.paypulse.repository.transaction;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TransactionWhereClause {

    private final StringBuilder sql = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    private TransactionWhereClause() {
    }

    static TransactionWhereClause of(TransactionSearchQuery query) {
        TransactionWhereClause where = new TransactionWhereClause();
        where.params.put("organisationId", query.organisationId());
        where.params.put("start", utc(query.start()));
        where.params.put("end", utc(query.end()));
        where.addIn("t.status", "statuses", query.statuses());
        where.addRawDataMatch("type", "type", query.types());
        where.addRawDataMatch("card_brand", "cardBrand", query.cardBrands());
        where.addRawDataMatch("sales_channel", "salesChannel", query.salesChannels());
        where.addEquals("t.raw_data ->> 'auth_code'", "authCode", query.authCode());
        where.addEquals("t.raw_data ->> 'rrn'", "rrn", query.rrn());
        where.addEquals("o.id_outlet", "outletId", query.outletId());
        where.addEquals("te.terminal_id", "terminalId", query.terminalId());
        return where;
    }

    String sql() {
        return sql.toString();
    }

    Map<String, Object> params() {
        return Map.copyOf(params);
    }

    private void addIn(String column, String param, List<? extends Enum<?>> values) {
        if (values.isEmpty()) {
            return;
        }
        sql.append(" AND ").append(column).append(" IN (:").append(param).append(')');
        params.put(param, values.stream().map(Enum::name).distinct().toList());
    }

    private void addRawDataMatch(String key, String paramPrefix, List<? extends Enum<?>> values) {
        if (values.isEmpty()) {
            return;
        }
        List<String> conditions = new ArrayList<>();
        List<String> names = values.stream().map(Enum::name).distinct().toList();
        for (int i = 0; i < names.size(); i++) {
            String param = paramPrefix + i;
            conditions.add("t.raw_data @> CAST(:" + param + " AS jsonb)");
            params.put(param, "{\"" + key + "\":\"" + names.get(i) + "\"}");
        }
        sql.append(" AND (").append(String.join(" OR ", conditions)).append(')');
    }

    private void addEquals(String expression, String param, Object value) {
        if (value == null) {
            return;
        }
        sql.append(" AND ").append(expression).append(" = :").append(param);
        params.put(param, value);
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
