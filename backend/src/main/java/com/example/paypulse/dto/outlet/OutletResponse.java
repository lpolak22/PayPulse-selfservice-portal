package com.example.paypulse.dto.outlet;

public record OutletResponse(
        Long id,
        String outletId,
        String name,
        String address,
        String city,
        String postalCode,
        String status,
        long terminalCount
) {
}
