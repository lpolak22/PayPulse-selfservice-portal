package com.example.paypulse.dto.outlet;

public record OutletSearchCriteria(String search, String city, String status) {

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }

    public boolean hasCity() {
        return city != null && !city.isBlank();
    }

    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }
}
