package com.example.paypulse.dto.merchant;

public record MerchantResponse(
        Long id,
        String name,
        String registrationNumber,
        String status,
        long outletCount
) {
}
