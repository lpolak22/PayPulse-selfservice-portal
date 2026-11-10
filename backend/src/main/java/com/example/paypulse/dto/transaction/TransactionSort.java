package com.example.paypulse.dto.transaction;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.repository.transaction.TransactionSortField;
import org.springframework.data.domain.Sort;

public record TransactionSort(TransactionSortField field, Sort.Direction direction) {

    public static TransactionSort of(String sort, String direction) {
        TransactionSortField field = TransactionSortField.fromParam(sort)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "sort must be date or amount."));
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "direction must be asc or desc."));
        return new TransactionSort(field, sortDirection);
    }
}
