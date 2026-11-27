package com.example.paypulse.mapper;

import com.example.paypulse.dto.transaction.TransactionCardResponse;
import com.example.paypulse.dto.transaction.TransactionDetailsResponse;
import com.example.paypulse.dto.transaction.TransactionMerchantResponse;
import com.example.paypulse.dto.transaction.TransactionOutletResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.dto.transaction.TransactionTerminalResponse;
import com.example.paypulse.repository.transaction.TransactionDetailsRow;
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
                CardMasking.mask(row.maskedPan()),
                row.salesChannel(),
                row.outletName(),
                row.terminalId(),
                row.authCode(),
                row.rrn()
        );
    }

    public TransactionDetailsResponse toDetailsResponse(TransactionDetailsRow row) {
        return new TransactionDetailsResponse(
                row.id(),
                row.transactionAt(),
                row.amount(),
                row.currency(),
                row.status(),
                row.type(),
                row.salesChannel(),
                row.authCode(),
                row.rrn(),
                row.acquirerName(),
                toCard(row),
                toTerminal(row),
                toOutlet(row),
                toMerchant(row)
        );
    }

    private TransactionCardResponse toCard(TransactionDetailsRow row) {
        return new TransactionCardResponse(row.cardBrand(), CardMasking.mask(row.maskedPan()));
    }

    private TransactionTerminalResponse toTerminal(TransactionDetailsRow row) {
        return new TransactionTerminalResponse(row.idTerminal(), row.terminalId(), row.terminalType());
    }

    private TransactionOutletResponse toOutlet(TransactionDetailsRow row) {
        return new TransactionOutletResponse(row.idOutlet(), row.outletName(), row.outletAddress(),
                row.outletCity(), row.outletPostalCode());
    }

    private TransactionMerchantResponse toMerchant(TransactionDetailsRow row) {
        return new TransactionMerchantResponse(row.idMerchant(), row.merchantName());
    }
}
