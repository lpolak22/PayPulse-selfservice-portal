package com.example.paypulse.repository.transaction;

import java.util.Arrays;
import java.util.Optional;

public enum TransactionSortField {
    DATE("t.transaction_at"),
    AMOUNT("t.amount");

    private final String column;

    TransactionSortField(String column) {
        this.column = column;
    }

    public String column() {
        return column;
    }

    public static Optional<TransactionSortField> fromParam(String value) {
        return Arrays.stream(values())
                .filter(field -> field.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
