package com.example.paypulse.dto.merchant;

public record MerchantSearchCriteria(String search, String oib, String status) {

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }

    public boolean hasOib() {
        return oib != null && !oib.isBlank();
    }

    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }
}
