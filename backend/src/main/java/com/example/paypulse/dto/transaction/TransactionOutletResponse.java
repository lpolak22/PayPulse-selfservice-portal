package com.example.paypulse.dto.transaction;

public record TransactionOutletResponse(
        long id,
        String name,
        String address,
        String city,
        String postalCode
) {
}
