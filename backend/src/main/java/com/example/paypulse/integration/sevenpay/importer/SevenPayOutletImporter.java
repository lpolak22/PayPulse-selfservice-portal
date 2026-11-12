package com.example.paypulse.integration.sevenpay.importer;

import com.example.paypulse.integration.sevenpay.SevenPayClient;
import com.example.paypulse.integration.sevenpay.SevenPayPages;
import com.example.paypulse.integration.sevenpay.dto.SevenPayAddress;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.OrganisationScope;
import com.example.paypulse.repository.OutletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SevenPayOutletImporter {

    private static final Logger log = LoggerFactory.getLogger(SevenPayOutletImporter.class);

    private final SevenPayClient sevenPayClient;
    private final MerchantRepository merchantRepository;
    private final OutletRepository outletRepository;

    public SevenPayOutletImporter(SevenPayClient sevenPayClient,
                                  MerchantRepository merchantRepository,
                                  OutletRepository outletRepository) {
        this.sevenPayClient = sevenPayClient;
        this.merchantRepository = merchantRepository;
        this.outletRepository = outletRepository;
    }

    @Transactional
    public ImportResult importOutlets(Organisation organisation) {
        Map<String, Merchant> merchantsByOib = merchantRepository
                .findAll(OrganisationScope.merchants(organisation.getIdOrganisation())).stream()
                .collect(Collectors.toMap(Merchant::getRegistrationNumber, Function.identity()));
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (SevenPaySalepoint source : SevenPayPages.fetchAll(sevenPayClient::getSalepoints)) {
            Merchant merchant = merchantsByOib.get(source.merchantOib());
            if (merchant == null) {
                log.warn("Skipping salepoint {}: merchant {} is not imported", source.mid(), source.merchantOib());
                skipped++;
                continue;
            }
            Optional<Outlet> existing = outletRepository.findBySourceId(source.mid());
            Outlet outlet = existing.orElseGet(Outlet::new);
            apply(outlet, merchant, source);
            outletRepository.save(outlet);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new ImportResult("outlets", created, updated, skipped);
    }

    private static void apply(Outlet outlet, Merchant merchant, SevenPaySalepoint source) {
        String status = SevenPayStatus.toLocal(source.status());
        outlet.setMerchant(merchant);
        outlet.setSourceId(source.mid());
        outlet.setName(source.name());
        outlet.setStatus(status);
        outlet.setIsActive(SevenPayStatus.ACTIVE.equals(status));
        SevenPayAddress address = source.address();
        if (address != null) {
            outlet.setAddress(address.streetAddressNumber());
            outlet.setCity(address.city());
            outlet.setPostalCode(address.zipCode());
            outlet.setCountry(address.country());
        }
    }
}
