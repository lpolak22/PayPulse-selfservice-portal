package com.example.paypulse.dto.merchant;

import java.util.Arrays;
import java.util.Optional;

public enum MerchantSortField {
    NAME("name"),
    OIB("registrationNumber"),
    STATUS("status");

    private final String property;

    MerchantSortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static Optional<MerchantSortField> fromParam(String value) {
        return Arrays.stream(values())
                .filter(field -> field.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
