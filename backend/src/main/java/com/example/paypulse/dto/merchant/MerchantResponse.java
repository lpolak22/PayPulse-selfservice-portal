package com.example.paypulse.dto.merchant;

public record MerchantResponse(
        Long id,
        String merchantId,
        String name,
        String registrationNumber,
        String status,
        long outletCount,
        long terminalCount
) {
}
