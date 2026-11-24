package com.example.paypulse.support;

import com.example.paypulse.integration.sevenpay.SevenPayClient;
import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.integration.sevenpay.dto.SevenPayPage;
import com.example.paypulse.integration.sevenpay.dto.SevenPayPageInfo;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransaction;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeSevenPayClient implements SevenPayClient {

    public final List<SevenPayMerchant> merchants = new ArrayList<>();
    public final List<SevenPaySalepoint> salepoints = new ArrayList<>();
    public final List<SevenPayTerminal> terminals = new ArrayList<>();
    public final List<String> oibLookups = new ArrayList<>();
    public final Map<String, List<SevenPayTransactionDetails>> transactionsByOib = new HashMap<>();
    public final List<String> detailLookups = new ArrayList<>();
    public RuntimeException failure;
    public int pageSize = 100;

    @Override
    public SevenPayPage<SevenPayMerchant> getMerchants(int page) {
        failIfSet();
        return page(merchants, page);
    }

    @Override
    public SevenPayMerchant getMerchantByOib(String oib) {
        oibLookups.add(oib);
        return merchants.stream().filter(merchant -> merchant.oib().equals(oib)).findFirst().orElseThrow();
    }

    @Override
    public SevenPayPage<SevenPaySalepoint> getSalepoints(int page) {
        return page(salepoints, page);
    }

    @Override
    public SevenPayPage<SevenPayTerminal> getTerminals(int page) {
        return page(terminals, page);
    }

    @Override
    public SevenPayPage<SevenPayTransaction> searchTransactions(SevenPayTransactionQuery query, int page) {
        failIfSet();
        List<SevenPayTransaction> found = transactionsByOib.getOrDefault(query.merchantOib(), List.of()).stream()
                .filter(details -> !details.timestamp().isBefore(query.timeFrom()))
                .filter(details -> !details.timestamp().isAfter(query.timeUntil()))
                .map(FakeSevenPayClient::summary)
                .toList();
        return page(found, page);
    }

    @Override
    public SevenPayTransactionDetails getTransactionDetails(String transactionId) {
        detailLookups.add(transactionId);
        return transactionsByOib.values().stream()
                .flatMap(List::stream)
                .filter(details -> details.transactionId().equals(transactionId))
                .findFirst()
                .orElseThrow();
    }

    private void failIfSet() {
        if (failure != null) {
            throw failure;
        }
    }

    private static SevenPayTransaction summary(SevenPayTransactionDetails details) {
        return new SevenPayTransaction(details.transactionId(), details.transactionType(), details.amount(),
                details.currency(), details.validity(), details.authorizationState(), details.responseCode(),
                details.authNumber(), details.acquirerHost(), details.acquirerTid(), details.acquirerMid(),
                details.timestamp());
    }

    private <T> SevenPayPage<T> page(List<T> all, int page) {
        int totalPages = Math.max(1, (all.size() + pageSize - 1) / pageSize);
        int from = Math.min(page * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new SevenPayPage<>(List.copyOf(all.subList(from, to)),
                new SevenPayPageInfo(pageSize, all.size(), totalPages, page));
    }
}
