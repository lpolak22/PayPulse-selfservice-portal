package com.example.paypulse.dto.merchant;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import org.springframework.data.domain.Sort;

public record MerchantSort(MerchantSortField field, Sort.Direction direction) {

    public static MerchantSort of(String sort, String direction) {
        MerchantSortField field = MerchantSortField.fromParam(sort)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "sort must be name, oib or status"));
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "direction must be asc or desc"));
        return new MerchantSort(field, sortDirection);
    }

    public Sort toSort() {
        return Sort.by(direction, field.property()).and(Sort.by("name", "idMerchant"));
    }
}
