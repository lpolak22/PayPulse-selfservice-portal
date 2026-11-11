package com.example.paypulse.seed;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.MccCode;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.seed.generator.TransactionGenerator;
import com.example.paypulse.seed.service.AcquirerSeedService;
import com.example.paypulse.seed.service.MccCodeSeedService;
import com.example.paypulse.seed.service.MerchantSeedService;
import com.example.paypulse.seed.service.OrganisationSeedService;
import com.example.paypulse.seed.service.OutletSeedService;
import com.example.paypulse.seed.service.TerminalSeedService;
import com.example.paypulse.seed.service.UserSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("seed")
public class SeedOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(SeedOrchestrator.class);

    private final OrganisationSeedService organisationSeedService;
    private final UserSeedService userSeedService;
    private final AcquirerSeedService acquirerSeedService;
    private final MccCodeSeedService mccCodeSeedService;
    private final MerchantSeedService merchantSeedService;
    private final OutletSeedService outletSeedService;
    private final TerminalSeedService terminalSeedService;
    private final TransactionGenerator transactionGenerator;

    public SeedOrchestrator(OrganisationSeedService organisationSeedService,
                            UserSeedService userSeedService,
                            AcquirerSeedService acquirerSeedService,
                            MccCodeSeedService mccCodeSeedService,
                            MerchantSeedService merchantSeedService,
                            OutletSeedService outletSeedService,
                            TerminalSeedService terminalSeedService,
                            TransactionGenerator transactionGenerator) {
        this.organisationSeedService = organisationSeedService;
        this.userSeedService = userSeedService;
        this.acquirerSeedService = acquirerSeedService;
        this.mccCodeSeedService = mccCodeSeedService;
        this.merchantSeedService = merchantSeedService;
        this.outletSeedService = outletSeedService;
        this.terminalSeedService = terminalSeedService;
        this.transactionGenerator = transactionGenerator;
    }

    public void orchestrate() {
        log.info("Starting seed");

        Organisation org1 = organisationSeedService.createOrganisation("Zagreb Retail Group d.o.o.");
        Organisation org2 = organisationSeedService.createOrganisation("Split Trade Solutions d.o.o.");

        userSeedService.createUsersForOrganisation(org1);
        userSeedService.createUsersForOrganisation(org2);

        Acquirer acq1 = acquirerSeedService.createAcquirer("ACQ001", "First Croatian Acquirer d.o.o.");
        Acquirer acq2 = acquirerSeedService.createAcquirer("ACQ002", "Adriatic Payment Services d.o.o.");
        Acquirer acq3 = acquirerSeedService.createAcquirer("ACQ003", "Central Payment Solutions d.o.o.");
        List<Acquirer> acquirers = Arrays.asList(acq1, acq2, acq3);

        MccCode mcc1 = mccCodeSeedService.createMccCode("5411", "Grocery Stores, Supermarkets");
        MccCode mcc2 = mccCodeSeedService.createMccCode("5812", "Eating Places, Restaurants");
        MccCode mcc3 = mccCodeSeedService.createMccCode("5541", "Service Stations");
        MccCode mcc4 = mccCodeSeedService.createMccCode("5999", "Miscellaneous Retail Stores");
        MccCode mcc5 = mccCodeSeedService.createMccCode("5732", "Electronics Stores");
        List<MccCode> mccCodes = Arrays.asList(mcc1, mcc2, mcc3, mcc4, mcc5);

        List<Merchant> merchants = new ArrayList<>();
        merchants.addAll(merchantSeedService.createMerchantsForOrganisation(org1, "ORG1"));
        merchants.addAll(merchantSeedService.createMerchantsForOrganisation(org2, "ORG2"));

        List<Outlet> outlets = outletSeedService.createOutlets(merchants);

        List<Terminal> terminals = terminalSeedService.createTerminals(outlets, mccCodes);

        transactionGenerator.generateTransactions(terminals, acquirers);

        log.info("Seed completed successfully");
    }
}
