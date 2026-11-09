package com.example.paypulse.dto.transaction;

import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.SalesChannel;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record TransactionSearchCriteria(
        OffsetDateTime from,
        OffsetDateTime to,
        List<TransactionStatus> statuses,
        List<TransactionType> types,
        List<CardBrand> cardBrands,
        List<SalesChannel> salesChannels,
        String authCode,
        String rrn,
        Long outletId,
        String terminalId
) {

    public TransactionSearchCriteria {
        statuses = distinct(statuses);
        types = distinct(types);
        cardBrands = distinct(cardBrands);
        salesChannels = distinct(salesChannels);
        authCode = blankToNull(authCode);
        rrn = blankToNull(rrn);
        terminalId = blankToNull(terminalId);
    }

    private static <T> List<T> distinct(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
