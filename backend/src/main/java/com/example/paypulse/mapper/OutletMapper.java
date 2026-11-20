package com.example.paypulse.mapper;

import com.example.paypulse.dto.merchant.MerchantDetailsResponse;
import com.example.paypulse.dto.outlet.OutletResponse;
import com.example.paypulse.model.Outlet;
import org.springframework.stereotype.Component;

@Component
public class OutletMapper {

    public OutletResponse toResponse(Outlet outlet, long terminalCount) {
        return new OutletResponse(
                outlet.getIdOutlet(),
                outlet.getSourceId(),
                outlet.getName(),
                outlet.getAddress(),
                outlet.getCity(),
                outlet.getPostalCode(),
                outlet.getStatus(),
                terminalCount
        );
    }

    public MerchantDetailsResponse.OutletItem toItem(Outlet outlet) {
        return new MerchantDetailsResponse.OutletItem(
                outlet.getIdOutlet(),
                outlet.getName(),
                outlet.getCity(),
                outlet.getStatus()
        );
    }
}
