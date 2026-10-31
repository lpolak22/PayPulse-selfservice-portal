package com.example.paypulse.seed.service;

import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.MerchantRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Profile("seed")
public class MerchantSeedService {
    private final MerchantRepository merchantRepository;

    public MerchantSeedService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public List<Merchant> createMerchantsForOrganisation(Organisation org, String prefix) {
        List<Merchant> merchants = new ArrayList<>();
        String[] merchantNames = {
            "Market Pro d.o.o.", "Fresh Foods d.o.o.", "Quick Shop d.o.o."
        };
        for (int i = 0; i < merchantNames.length; i++) {
            String regNum = prefix + String.format("%07d", i + 1000);
            Optional<Merchant> existing = merchantRepository.findByRegistrationNumber(regNum);
            if (existing.isPresent()) {
                merchants.add(existing.get());
                continue;
            }
            Merchant merchant = new Merchant();
            merchant.setOrganisation(org);
            merchant.setSourceId(prefix + "-M" + i);
            String baseName = org.getName().split(" ")[0];
            merchant.setName(merchantNames[i] + " " + baseName);
            merchant.setRegistrationNumber(regNum);
            merchant.setStatus("ACTIVE");
            merchant = merchantRepository.save(merchant);
            merchants.add(merchant);
        }
        return merchants;
    }
}
