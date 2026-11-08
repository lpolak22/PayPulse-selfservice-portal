package com.example.paypulse.integration.sevenpay.importer;

import com.example.paypulse.config.SevenPayImportProperties;
import com.example.paypulse.integration.sevenpay.SevenPayClient;
import com.example.paypulse.integration.sevenpay.SevenPayPages;
import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SevenPayMerchantImporter {

    private final SevenPayClient sevenPayClient;
    private final MerchantRepository merchantRepository;
    private final SevenPayImportProperties properties;

    public SevenPayMerchantImporter(SevenPayClient sevenPayClient,
                                    MerchantRepository merchantRepository,
                                    SevenPayImportProperties properties) {
        this.sevenPayClient = sevenPayClient;
        this.merchantRepository = merchantRepository;
        this.properties = properties;
    }

    @Transactional
    public ImportResult importMerchants(Organisation organisation) {
        int created = 0;
        int updated = 0;
        for (SevenPayMerchant source : fetchMerchants()) {
            Optional<Merchant> existing = merchantRepository.findByRegistrationNumber(source.oib());
            Merchant merchant = existing.orElseGet(Merchant::new);
            merchant.setOrganisation(organisation);
            merchant.setSourceId(source.shortName());
            merchant.setName(source.fullName());
            merchant.setRegistrationNumber(source.oib());
            merchant.setStatus(SevenPayStatus.toLocal(source.status()));
            merchantRepository.save(merchant);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new ImportResult("merchants", created, updated);
    }

    private Collection<SevenPayMerchant> fetchMerchants() {
        Map<String, SevenPayMerchant> byOib = new LinkedHashMap<>();
        for (String oib : properties.merchantOibs()) {
            SevenPayMerchant merchant = sevenPayClient.getMerchantByOib(oib);
            byOib.put(merchant.oib(), merchant);
        }
        for (SevenPayMerchant merchant : SevenPayPages.fetchAll(sevenPayClient::getMerchants)) {
            byOib.putIfAbsent(merchant.oib(), merchant);
        }
        return byOib.values();
    }
}
