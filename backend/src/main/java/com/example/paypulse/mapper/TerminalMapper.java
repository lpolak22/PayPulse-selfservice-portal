package com.example.paypulse.mapper;

import com.example.paypulse.dto.merchant.MerchantDetailsResponse;
import com.example.paypulse.dto.terminal.TerminalResponse;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import org.springframework.stereotype.Component;

@Component
public class TerminalMapper {

    public TerminalResponse toResponse(Terminal terminal) {
        Outlet outlet = terminal.getOutlet();
        Merchant merchant = outlet.getMerchant();
        return new TerminalResponse(
                terminal.getIdTerminal(),
                terminal.getTerminalId(),
                terminal.getType(),
                terminal.getStatus(),
                new TerminalResponse.OutletRef(outlet.getIdOutlet(), outlet.getName()),
                new TerminalResponse.MerchantRef(merchant.getIdMerchant(), merchant.getName())
        );
    }

    public MerchantDetailsResponse.TerminalItem toItem(Terminal terminal) {
        Outlet outlet = terminal.getOutlet();
        return new MerchantDetailsResponse.TerminalItem(
                terminal.getIdTerminal(),
                terminal.getTerminalId(),
                new TerminalResponse.OutletRef(outlet.getIdOutlet(), outlet.getName()),
                terminal.getStatus()
        );
    }
}
