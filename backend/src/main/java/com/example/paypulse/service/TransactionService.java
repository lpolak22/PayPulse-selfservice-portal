package com.example.paypulse.service;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.dto.transaction.TransactionSearchCriteria;
import com.example.paypulse.dto.transaction.TransactionSort;

public interface TransactionService {

    PageResponse<TransactionResponse> search(TransactionSearchCriteria criteria, TransactionSort sort,
                                             PageQuery pageQuery);
}
