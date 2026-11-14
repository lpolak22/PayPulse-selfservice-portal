package com.example.paypulse.mapper;

import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.model.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantResponse toResponse(Merchant merchant, long outletCount, long terminalCount) {
        return new MerchantResponse(
                merchant.getIdMerchant(),
                merchant.getSourceId(),
                merchant.getName(),
                merchant.getRegistrationNumber(),
                merchant.getStatus(),
                outletCount,
                terminalCount
        );
    }
}
