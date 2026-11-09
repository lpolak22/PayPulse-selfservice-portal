package com.example.paypulse.mapper;

import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.repository.transaction.TransactionRow;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionRow row) {
        return new TransactionResponse(
                row.id(),
                row.transactionAt(),
                row.amount(),
                row.currency(),
                row.status(),
                row.type(),
                row.cardBrand(),
                row.maskedPan(),
                row.salesChannel(),
                row.outletName(),
                row.terminalId(),
                row.authCode(),
                row.rrn()
        );
    }
}
