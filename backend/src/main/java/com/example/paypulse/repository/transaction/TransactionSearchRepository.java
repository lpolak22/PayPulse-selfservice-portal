package com.example.paypulse.repository.transaction;

import java.util.List;

public interface TransactionSearchRepository {

    List<TransactionRow> findPage(TransactionSearchQuery query);

    long count(TransactionSearchQuery query);
}
