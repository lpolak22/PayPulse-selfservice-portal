package com.example.paypulse.repository.transaction;

import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.SalesChannel;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

public record TransactionSearchQuery(
        long organisationId,
        Instant start,
        Instant end,
        List<TransactionStatus> statuses,
        List<TransactionType> types,
        List<CardBrand> cardBrands,
        List<SalesChannel> salesChannels,
        String authCode,
        String rrn,
        Long outletId,
        String terminalId,
        TransactionSortField sortField,
        Sort.Direction direction,
        int limit,
        long offset
) {

    public TransactionSearchQuery {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
        types = types == null ? List.of() : List.copyOf(types);
        cardBrands = cardBrands == null ? List.of() : List.copyOf(cardBrands);
        salesChannels = salesChannels == null ? List.of() : List.copyOf(salesChannels);
    }
}
