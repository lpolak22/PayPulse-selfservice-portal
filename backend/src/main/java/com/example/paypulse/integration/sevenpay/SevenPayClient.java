package com.example.paypulse.integration.sevenpay;

import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.integration.sevenpay.dto.SevenPayPage;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransaction;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionQuery;

public interface SevenPayClient {

    SevenPayPage<SevenPayMerchant> getMerchants(int page);

    SevenPayPage<SevenPaySalepoint> getSalepoints(int page);

    SevenPayPage<SevenPayTerminal> getTerminals(int page);

    SevenPayPage<SevenPayTransaction> searchTransactions(SevenPayTransactionQuery query, int page);

    SevenPayTransactionDetails getTransactionDetails(String transactionId);
}
