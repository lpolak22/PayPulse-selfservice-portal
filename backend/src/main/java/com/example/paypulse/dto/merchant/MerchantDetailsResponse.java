package com.example.paypulse.dto.merchant;

import com.example.paypulse.dto.terminal.TerminalResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MerchantDetailsResponse(
        Long id,
        String merchantId,
        String name,
        String registrationNumber,
        String status,
        RelatedItems<OutletItem> outlets,
        RelatedItems<TerminalItem> terminals
) {

    public record RelatedItems<T>(
            @Schema(description = "At most 50 items")
            List<T> items,
            @Schema(description = "Number of all items, also those left out of items")
            long total,
            @Schema(description = "Paginated endpoint that lists all items")
            String href
    ) {

        public RelatedItems {
            items = List.copyOf(items);
        }
    }

    public record OutletItem(Long id, String name, String city, String status) {
    }

    public record TerminalItem(Long id, String terminalId, TerminalResponse.OutletRef outlet, String status) {
    }
}
