package com.example.paypulse.mapper;

import com.example.paypulse.dto.merchant.MerchantDetailsResponse;
import com.example.paypulse.dto.merchant.MerchantDetailsResponse.OutletItem;
import com.example.paypulse.dto.merchant.MerchantDetailsResponse.RelatedItems;
import com.example.paypulse.dto.merchant.MerchantDetailsResponse.TerminalItem;
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

    public MerchantDetailsResponse toDetailsResponse(Merchant merchant, RelatedItems<OutletItem> outlets,
                                                     RelatedItems<TerminalItem> terminals) {
        return new MerchantDetailsResponse(
                merchant.getIdMerchant(),
                merchant.getSourceId(),
                merchant.getName(),
                merchant.getRegistrationNumber(),
                merchant.getStatus(),
                outlets,
                terminals
        );
    }
}
