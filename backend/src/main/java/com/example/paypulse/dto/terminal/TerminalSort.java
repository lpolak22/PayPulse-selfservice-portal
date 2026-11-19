package com.example.paypulse.dto.terminal;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import org.springframework.data.domain.Sort;

public record TerminalSort(TerminalSortField field, Sort.Direction direction) {

    public static TerminalSort of(String sort, String direction) {
        TerminalSortField field = TerminalSortField.fromParam(sort)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST,
                        "sort must be tid, type, status, outlet or merchant"));
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "direction must be asc or desc"));
        return new TerminalSort(field, sortDirection);
    }

    public Sort toSort() {
        Sort sort = Sort.by(direction, field.property());
        if (field != TerminalSortField.TID) {
            sort = sort.and(Sort.by("terminalId"));
        }
        return sort.and(Sort.by("idTerminal"));
    }
}
