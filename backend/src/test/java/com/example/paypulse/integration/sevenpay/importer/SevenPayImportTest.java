package com.example.paypulse.integration.sevenpay.importer;

import com.example.paypulse.config.SevenPayImportProperties;
import com.example.paypulse.integration.sevenpay.dto.SevenPayAddress;
import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.MccCodeRepository;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.FakeSevenPayClient;
import com.example.paypulse.support.TransactionFixtures;
import com.example.paypulse.support.TransactionFixtures.TestTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class SevenPayImportTest extends ApiIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private MccCodeRepository mccCodeRepository;

    private final FakeSevenPayClient client = new FakeSevenPayClient();
    private Organisation organisation;
    private String merchantOib;
    private String outletMid;

    @BeforeEach
    void setUp() {
        organisation = organisationRepository.findById(fixtures.organisation()).orElseThrow();
        merchantOib = oib();
        outletMid = unique("MID");
        client.merchants.add(new SevenPayMerchant("SEVENPAY", "7 PAYMENTS d.o.o", merchantOib, "active", null));
        client.salepoints.add(new SevenPaySalepoint(outletMid, "Zagreb Centar", merchantOib, "ACTIVE",
                new SevenPayAddress("191", "Zagreb", "Ilica 1", "10000")));
    }

    @Test
    void merchantsAreImportedWithShortNameAsMerchantId() {
        ImportResult result = importMerchants();

        assertThat(result.created()).isEqualTo(1);
        assertThat(row("select source_id, name, status from merchant where registration_number = ?", merchantOib))
                .containsEntry("source_id", "SEVENPAY")
                .containsEntry("name", "7 PAYMENTS d.o.o")
                .containsEntry("status", "ACTIVE");
    }

    @Test
    void secondImportUpdatesInsteadOfDuplicating() {
        importMerchants();
        client.merchants.set(0, new SevenPayMerchant("SEVENPAY", "7 PAYMENTS novo", merchantOib, "HOLD", null));

        ImportResult result = importMerchants();

        assertThat(result.updated()).isEqualTo(1);
        assertThat(count("select count(*) from merchant where registration_number = ?", merchantOib)).isEqualTo(1);
        assertThat(row("select name, status from merchant where registration_number = ?", merchantOib))
                .containsEntry("name", "7 PAYMENTS novo")
                .containsEntry("status", "HOLD");
    }

    @Test
    void configuredOibIsFetchedDirectlyAndNotImportedTwice() {
        SevenPayMerchantImporter importer = new SevenPayMerchantImporter(client, merchantRepository,
                new SevenPayImportProperties(organisation.getName(), List.of(merchantOib)));

        ImportResult result = importer.importMerchants(organisation);

        assertThat(client.oibLookups).containsExactly(merchantOib);
        assertThat(result.created()).isEqualTo(1);
    }

    @Test
    void salepointsBecomeOutletsWithTheirAddress() {
        importMerchants();

        ImportResult result = importOutlets();

        assertThat(result.created()).isEqualTo(1);
        assertThat(row("select name, city, country from outlet where source_id = ?", outletMid))
                .containsEntry("name", "Zagreb Centar")
                .containsEntry("city", "Zagreb")
                .containsEntry("country", "191");
    }

    @Test
    void salepointOfAMerchantThatIsNotImportedIsSkipped() {
        importMerchants();
        client.salepoints.add(new SevenPaySalepoint(unique("MID"), "Strani", oib(), "ACTIVE", null));

        assertThat(importOutlets().skipped()).isEqualTo(1);
    }

    @Test
    void onlySoftPosV1AndV2TerminalsAreImported() {
        importMerchantsAndOutlets();
        String v1 = addTerminal("SoftPOS V1", "5411");
        String v2 = addTerminal("softpos v2", "5411");
        addTerminal("SATURN 1000 Mobile", "5411");
        addTerminal("ICT22x", "5411");
        addTerminal(null, "5411");

        ImportResult result = importTerminals();

        assertThat(result.created()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(3);
        assertThat(importedTerminalIds()).containsExactlyInAnyOrder(v1, v2);
    }

    @Test
    void threeAndFourDigitMccCodesAndRangesAreAccepted() {
        importMerchantsAndOutlets();
        addTerminal("SoftPOS V1", "742");
        addTerminal("SoftPOS V1", "5411");
        addTerminal("SoftPOS V1", "3000-3299");
        addTerminal("SoftPOS V1", "12");
        addTerminal("SoftPOS V1", "ABCD");
        addTerminal("SoftPOS V1", null);

        ImportResult result = importTerminals();

        assertThat(result.created()).isEqualTo(3);
        assertThat(result.skipped()).isEqualTo(3);
        assertThat(mccCodeRepository.existsById("742")).isTrue();
    }

    @Test
    void previouslyImportedTerminalsOfOtherTypesAreRemoved() {
        importMerchantsAndOutlets();
        String oldSaturn = insertTerminal("SATURN 1000 Countertop ECR F3");
        String softPos = addTerminal("SoftPOS V2", "5411");

        importTerminals();

        assertThat(importedTerminalIds()).containsExactly(softPos).doesNotContain(oldSaturn);
    }

    @Test
    void terminalOfAnotherTypeIsKeptWhenItHasTransactions() {
        importMerchantsAndOutlets();
        String saturn = insertTerminal("SATURN 1000 Mobile");
        TransactionFixtures transactions = new TransactionFixtures(jdbc);
        long terminalId = count("select id_terminal from terminal where terminal_id = ?", saturn);
        transactions.add(TestTransaction.approvedEurSale(terminalId, transactions.acquirer(), Instant.now(), "10.00"));

        importTerminals();

        assertThat(importedTerminalIds()).contains(saturn);
    }

    @Test
    void terminalOfAnOutletThatIsNotImportedIsSkipped() {
        importMerchantsAndOutlets();
        client.terminals.add(new SevenPayTerminal(unique("T"), "SoftPOS V1", "ACTIVE", unique("MID"), "5411",
                "EUR", List.of()));

        assertThat(importTerminals().skipped()).isEqualTo(1);
    }

    @Test
    void terminalsOnSeveralPagesAreAllImported() {
        importMerchantsAndOutlets();
        client.pageSize = 2;
        for (int i = 0; i < 5; i++) {
            addTerminal("SoftPOS V1", "5411");
        }

        assertThat(importTerminals().created()).isEqualTo(5);
    }

    @Test
    void deactivatingATerminalSetsItsDeactivationDate() {
        importMerchantsAndOutlets();
        String tid = addTerminal("SoftPOS V1", "5411");
        importTerminals();
        client.terminals.set(0, new SevenPayTerminal(tid, "SoftPOS V1", "INACTIVE", outletMid, "5411", "EUR",
                List.of()));

        ImportResult result = importTerminals();

        assertThat(result.updated()).isEqualTo(1);
        assertThat(row("select status, deactivated_at from terminal where terminal_id = ?", tid))
                .containsEntry("status", "INACTIVE")
                .extractingByKey("deactivated_at").isNotNull();
    }

    private ImportResult importMerchants() {
        return new SevenPayMerchantImporter(client, merchantRepository,
                new SevenPayImportProperties(organisation.getName(), List.of())).importMerchants(organisation);
    }

    private ImportResult importOutlets() {
        return new SevenPayOutletImporter(client, merchantRepository, outletRepository).importOutlets(organisation);
    }

    private ImportResult importTerminals() {
        return new SevenPayTerminalImporter(client, outletRepository, terminalRepository, mccCodeRepository)
                .importTerminals(organisation);
    }

    private void importMerchantsAndOutlets() {
        importMerchants();
        importOutlets();
    }

    private String addTerminal(String type, String mcc) {
        String tid = unique("T");
        client.terminals.add(new SevenPayTerminal(tid, type, "ACTIVE", outletMid, mcc, "EUR", List.of()));
        return tid;
    }

    private String insertTerminal(String type) {
        String tid = unique("T");
        jdbc.update("insert into mcc_code (mcc_code, description) values ('5411', 'Test') on conflict do nothing");
        jdbc.update("""
                insert into terminal (id_outlet, mcc_code, source_id, terminal_id, type, status)
                select id_outlet, '5411', ?, ?, ?, 'ACTIVE' from outlet where source_id = ?
                """, tid, tid, type, outletMid);
        return tid;
    }

    private List<String> importedTerminalIds() {
        terminalRepository.flush();
        return jdbc.queryForList("""
                select t.terminal_id from terminal t
                join outlet o on o.id_outlet = t.id_outlet
                where o.source_id = ?
                """, String.class, outletMid);
    }

    private Map<String, Object> row(String sql, Object... args) {
        terminalRepository.flush();
        return jdbc.queryForMap(sql, args);
    }

    private long count(String sql, Object... args) {
        terminalRepository.flush();
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private static String oib() {
        StringBuilder oib = new StringBuilder("0");
        while (oib.length() < 11) {
            oib.append(ThreadLocalRandom.current().nextInt(10));
        }
        return oib.toString();
    }

    private static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
