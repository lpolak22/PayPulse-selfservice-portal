package com.example.paypulse.integration.sevenpay.importer;

import com.example.paypulse.config.SevenPayImportProperties;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.OrganisationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SevenPayImportService {

    private final OrganisationRepository organisationRepository;
    private final SevenPayMerchantImporter merchantImporter;
    private final SevenPayOutletImporter outletImporter;
    private final SevenPayImportProperties properties;

    public SevenPayImportService(OrganisationRepository organisationRepository,
                                 SevenPayMerchantImporter merchantImporter,
                                 SevenPayOutletImporter outletImporter,
                                 SevenPayImportProperties properties) {
        this.organisationRepository = organisationRepository;
        this.merchantImporter = merchantImporter;
        this.outletImporter = outletImporter;
        this.properties = properties;
    }

    public List<ImportResult> importAll() {
        Organisation organisation = organisationRepository.findByName(properties.organisation())
                .orElseThrow(() -> new IllegalStateException(
                        "Organisation '" + properties.organisation() + "' not found, run the seed first"));
        return List.of(
                merchantImporter.importMerchants(organisation),
                outletImporter.importOutlets(organisation));
    }
}
