package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.config.SevenPayImportProperties;
import com.example.paypulse.config.SevenPaySyncProperties;
import com.example.paypulse.integration.sevenpay.SevenPayException;
import com.example.paypulse.integration.sevenpay.dto.SevenPayAddress;
import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.importer.SevenPayImportService;
import com.example.paypulse.integration.sevenpay.importer.SevenPayMerchantImporter;
import com.example.paypulse.integration.sevenpay.importer.SevenPayOutletImporter;
import com.example.paypulse.integration.sevenpay.importer.SevenPayTerminalImporter;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.SyncRun;
import com.example.paypulse.repository.MccCodeRepository;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.repository.SyncRunRepository;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.repository.TransactionRepository;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.FakeSevenPayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class SevenPayTransactionSyncTest extends ApiIntegrationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);
    private static final LocalDateTime AT = DAY.atTime(10, 15, 30);

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
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private SyncRunRepository syncRunRepository;
    @Autowired
    private SevenPayTransactionMapper mapper;
    @Autowired
    private SevenPayTransactionWriter writer;

    private final FakeSevenPayClient client = new FakeSevenPayClient();
    private final List<SevenPayTransactionDetails> transactions = new ArrayList<>();
    private Organisation organisation;
    private String softPosTid;
    private String saturnTid;

    @BeforeEach
    void setUp() {
        organisation = organisationRepository.findById(fixtures.organisation()).orElseThrow();
        String merchantOib = oib();
        String outletMid = unique("MID");
        softPosTid = unique("T");
        saturnTid = unique("T");
        client.merchants.add(new SevenPayMerchant(unique("M"), "7 PAYMENTS test", merchantOib, "active", null));
        client.salepoints.add(new SevenPaySalepoint(outletMid, "Zagreb Centar", merchantOib, "ACTIVE",
                new SevenPayAddress("191", "Zagreb", "Ilica 1", "10000")));
        client.terminals.add(new SevenPayTerminal(softPosTid, "SoftPOS V2", "ACTIVE", outletMid, "5411", "EUR",
                List.of()));
        client.terminals.add(new SevenPayTerminal(saturnTid, "SATURN 1000 Mobile", "ACTIVE", outletMid, "5411",
                "EUR", List.of()));
        client.transactionsByOib.put(merchantOib, transactions);
        importService().importAll();
    }

    @Test
    void softPosTransactionIsStoredInThePortalFormat() {
        String id = add(sale(softPosTid));

        SyncCounts counts = sync();

        assertThat(counts.written(WriteOutcome.CREATED)).isEqualTo(1);
        assertThat(row(id))
                .containsEntry("currency", "EUR")
                .containsEntry("status", "APPROVED")
                .containsEntry("type", "SALE")
                .containsEntry("card_brand", "VISA")
                .containsEntry("masked_pan", "478560******9947")
                .containsEntry("transaction_at", "2026-11-16T10:15:30+01:00")
                .containsEntry("acquirer_code", "7PAY-03")
                .containsEntry("terminal_id", softPosTid);
        assertThat((BigDecimal) row(id).get("amount")).isEqualByComparingTo("12.50");
    }

    @Test
    void secondRunDoesNotDuplicateAndPicksUpALaterVoid() {
        String id = add(sale(softPosTid));
        sync();
        transactions.set(0, details(id, softPosTid, "sale", "voided", "visa"));

        SyncCounts counts = sync();

        assertThat(counts.written(WriteOutcome.UPDATED)).isEqualTo(1);
        transactionRepository.flush();
        assertThat(jdbc.queryForObject("select count(*) from transaction where source_id = ?", Long.class, id))
                .isEqualTo(1);
        assertThat(row(id)).containsEntry("status", "CANCELLED");
        assertThat(client.detailLookups).containsExactly(id);
    }

    @Test
    void dummyCardBrandsAreSkippedAndCounted() {
        String notKnown = add(details(unique("TX"), softPosTid, "sale", "valid", "not_known"));
        String closedLoop = add(details(unique("TX"), softPosTid, "sale", "valid", "closed_loop_prepaid"));

        SyncCounts counts = sync();

        assertThat(counts.skipped(SyncSkipReason.DUMMY_CARD_BRAND)).isEqualTo(2);
        assertThat(stored(notKnown)).isFalse();
        assertThat(stored(closedLoop)).isFalse();
    }

    @Test
    void transactionOfATerminalThatIsNotImportedIsSkipped() {
        String id = add(sale(saturnTid));

        SyncCounts counts = sync();

        assertThat(counts.skipped(SyncSkipReason.UNKNOWN_TERMINAL)).isEqualTo(1);
        assertThat(stored(id)).isFalse();
    }

    @Test
    void preauthorizationIsSkipped() {
        String id = add(details(unique("TX"), softPosTid, "preauthorization", "valid", "visa"));

        assertThat(sync().skipped(SyncSkipReason.UNSUPPORTED_TYPE)).isEqualTo(1);
        assertThat(stored(id)).isFalse();
    }

    @Test
    void unreachableSevenPayIsRecordedAsFailed() {
        client.failure = new SevenPayException("7Pay is unreachable", new IOException("timeout"));

        SyncRun run = syncService().runOnce().orElseThrow();

        assertThat(run.getStatus()).isEqualTo("FAILED");
        assertThat(run.getErrorMessage()).contains("7Pay is unreachable");
    }

    private SyncCounts sync() {
        SyncCounts counts = new SyncCounts();
        transactionSync().sync(organisation, DAY, DAY, counts);
        return counts;
    }

    private SevenPayTransactionSync transactionSync() {
        return new SevenPayTransactionSync(client, merchantRepository, terminalRepository, transactionRepository,
                mapper, writer);
    }

    private SevenPaySyncService syncService() {
        return new SevenPaySyncService(importService(), transactionSync(), organisationRepository, syncRunRepository,
                importProperties(), new SevenPaySyncProperties(true, 1, 1, null));
    }

    private SevenPayImportService importService() {
        return new SevenPayImportService(organisationRepository,
                new SevenPayMerchantImporter(client, merchantRepository, importProperties()),
                new SevenPayOutletImporter(client, merchantRepository, outletRepository),
                new SevenPayTerminalImporter(client, outletRepository, terminalRepository, mccCodeRepository),
                importProperties());
    }

    private SevenPayImportProperties importProperties() {
        return new SevenPayImportProperties(organisation.getName(), List.of());
    }

    private String add(SevenPayTransactionDetails details) {
        transactions.add(details);
        return details.transactionId();
    }

    private static SevenPayTransactionDetails sale(String tid) {
        return details(unique("TX"), tid, "sale", "valid", "visa");
    }

    private static SevenPayTransactionDetails details(String id, String tid, String type, String validity,
                                                      String brand) {
        return new SevenPayTransactionDetails(id, type, new BigDecimal("12.5"), "978", validity, "authorized", "00",
                "071035", "03", tid, "SED000000003", AT, tid, "SED000000003", "478560******9947", brand,
                "632016120310", null, "NO_RATES", null, null);
    }

    private Map<String, Object> row(String sourceId) {
        transactionRepository.flush();
        return jdbc.queryForMap("""
                select t.amount, t.currency, t.status, t.raw_data ->> 'type' as type,
                       t.raw_data ->> 'card_brand' as card_brand, t.raw_data ->> 'masked_pan' as masked_pan,
                       t.raw_data ->> 'transaction_at' as transaction_at, a.code as acquirer_code, te.terminal_id
                from transaction t
                join acquirer a on a.id_acquirer = t.id_acquirer
                join terminal te on te.id_terminal = t.id_terminal
                where t.source_id = ?
                """, sourceId);
    }

    private boolean stored(String sourceId) {
        return transactionRepository.existsBySourceId(sourceId);
    }

    private static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String oib() {
        StringBuilder oib = new StringBuilder("0");
        while (oib.length() < 11) {
            oib.append(ThreadLocalRandom.current().nextInt(10));
        }
        return oib.toString();
    }
}
