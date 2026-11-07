package com.example.paypulse.mapper;

import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.model.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantResponse toResponse(Merchant merchant) {
        return new MerchantResponse(
                merchant.getIdMerchant(),
                merchant.getName(),
                merchant.getRegistrationNumber(),
                merchant.getStatus()
        );
    }
}
