package com.example.paypulse.api;

import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.dashboard.AcquirerTotalResponse;
import com.example.paypulse.dto.dashboard.CardBrandTotalResponse;
import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;
import com.example.paypulse.dto.dashboard.LatestTransactionResponse;
import com.example.paypulse.dto.dashboard.TrendPointResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.TransactionFixtures;
import com.example.paypulse.support.TransactionFixtures.TestTransaction;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardDataApiTest extends ApiIntegrationTest {

    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 30);
    private static final Instant IN_RANGE = noonOf(LocalDate.of(2026, 10, 15));
    private static final Instant IN_PREVIOUS_PERIOD = noonOf(FROM.minusDays(5));

    private TransactionFixtures transactions;
    private long organisationId;
    private TestUser user;
    private long terminal;
    private long acquirer;

    @BeforeEach
    void setUp() {
        transactions = new TransactionFixtures(jdbc);
        organisationId = fixtures.organisation();
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        terminal = transactions.terminal(organisationId);
        acquirer = transactions.acquirer();
    }

    @Test
    void cardBrandAndAcquirerTotalsAddUpToTheOverallTotals() throws Exception {
        long otherAcquirer = transactions.acquirer();
        transactions.add(sale(IN_RANGE, "10.50"));
        transactions.add(sale(IN_RANGE, "20.25").withBrand(CardBrand.MASTERCARD).withAcquirer(otherAcquirer));
        transactions.add(sale(IN_RANGE, "5.00").withType(TransactionType.SALE_WITH_INSTALLMENTS)
                .withAcquirer(otherAcquirer));
        transactions.add(sale(IN_RANGE, "99.00").withType(TransactionType.REFUND));

        DashboardSummaryResponse summary = summary(user, FROM, TO);

        expectTotals(summary, "35.75", 3);
        assertThat(summary.byCardBrand()).hasSize(2);
        assertThat(summary.byAcquirer()).hasSize(2);
        assertThat(sum(summary.byCardBrand(), CardBrandTotalResponse::amount))
                .isEqualByComparingTo(summary.totalAmount());
        assertThat(sum(summary.byAcquirer(), AcquirerTotalResponse::amount))
                .isEqualByComparingTo(summary.totalAmount());
        assertThat(summary.byCardBrand().stream().mapToLong(CardBrandTotalResponse::count).sum())
                .isEqualTo(summary.totalCount());
        assertThat(summary.byAcquirer().stream().mapToLong(AcquirerTotalResponse::count).sum())
                .isEqualTo(summary.totalCount());
    }

    @Test
    void totalsEqualTheTransactionListForTheSameRangeAndRules() throws Exception {
        addCountedRowsAcrossTheRange();
        addRowsThatDoNotCount();

        DashboardSummaryResponse summary = summary(user, FROM, TO);
        List<TransactionResponse> sales = listedEurTransactions("APPROVED", "SALE", "SALE_WITH_INSTALLMENTS");
        List<TransactionResponse> declined = listedEurTransactions("DECLINED");

        expectTotals(summary, "111.10", 4);
        assertThat(summary.declinedCount()).isEqualTo(2);
        assertThat(sum(sales, TransactionResponse::amount)).isEqualByComparingTo(summary.totalAmount());
        assertThat(sales).hasSize((int) summary.totalCount());
        assertThat(declined).hasSize((int) summary.declinedCount());
    }

    @Test
    void transactionsOfAnotherOrganisationAreNotCounted() throws Exception {
        long otherOrganisation = fixtures.organisation();
        TestUser otherUser = fixtures.user(otherOrganisation, UserRole.USER, UserStatus.ACTIVE);
        long otherTerminal = transactions.terminal(otherOrganisation);
        transactions.add(sale(IN_RANGE, "10.00"));
        transactions.add(TestTransaction.approvedEurSale(otherTerminal, acquirer, IN_RANGE, "70.00")
                .withBrand(CardBrand.AMEX));
        transactions.add(TestTransaction.approvedEurSale(otherTerminal, acquirer, IN_RANGE, "80.00")
                .withStatus(TransactionStatus.DECLINED));

        DashboardSummaryResponse summary = summary(user, FROM, TO);
        DashboardSummaryResponse otherSummary = summary(otherUser, FROM, TO);

        expectTotals(summary, "10.00", 1);
        assertThat(summary.declinedCount()).isZero();
        assertThat(summary.byCardBrand()).extracting(CardBrandTotalResponse::brand).containsExactly("VISA");
        assertThat(summary.latestTransactions()).hasSize(1);
        expectTotals(otherSummary, "70.00", 1);
        assertThat(otherSummary.declinedCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11-10", "2026-07-15"})
    void midnightInZagrebSeparatesTheDays(LocalDate day) throws Exception {
        LocalDate nextDay = day.plusDays(1);
        transactions.add(sale(day.atTime(23, 59, 59).atZone(ZAGREB).toInstant(), "10.00"));
        transactions.add(sale(nextDay.atStartOfDay(ZAGREB).toInstant(), "20.00"));

        DashboardSummaryResponse firstDay = summary(user, day, day);
        DashboardSummaryResponse secondDay = summary(user, nextDay, nextDay);

        expectTotals(firstDay, "10.00", 1);
        expectTotals(secondDay, "20.00", 1);
        assertThat(secondDay.previousTotalAmount()).isEqualByComparingTo("10.00");
        assertThat(trendPoint(firstDay, day.atTime(23, 0)).count()).isEqualTo(1);
        assertThat(trendPoint(secondDay, nextDay.atStartOfDay()).count()).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @CsvSource({
            "APPROVED, REFUND, EUR",
            "APPROVED, STORNO, EUR",
            "APPROVED, TECHNICAL_REVERSAL, EUR",
            "APPROVED, SALE, USD",
            "APPROVED, SALE_WITH_INSTALLMENTS, USD",
            "CANCELLED, SALE, EUR",
            "REFUNDED, SALE, EUR"
    })
    void transactionsOutsideTheRulesAreNotInTheTurnover(TransactionStatus transactionStatus, TransactionType type,
                                                        String currency) throws Exception {
        for (Instant at : List.of(IN_RANGE, IN_PREVIOUS_PERIOD)) {
            transactions.add(sale(at, "50.00").withStatus(transactionStatus).withType(type).withCurrency(currency));
        }

        DashboardSummaryResponse summary = summary(user, FROM, TO);

        expectTotals(summary, "0.00", 0);
        assertThat(summary.previousTotalAmount()).isEqualByComparingTo("0.00");
        assertThat(summary.byCardBrand()).isEmpty();
        assertThat(summary.byAcquirer()).isEmpty();
        assertThat(summary.trend().points()).allSatisfy(point -> assertThat(point.count()).isZero());
    }

    @Test
    void approvedEurSalesOfBothTypesAreInTheTurnover() throws Exception {
        transactions.add(sale(IN_RANGE, "10.00"));
        transactions.add(sale(IN_RANGE, "20.00").withType(TransactionType.SALE_WITH_INSTALLMENTS));

        expectTotals(summary(user, FROM, TO), "30.00", 2);
    }

    @Test
    void declinedCountIncludesOnlyEurTransactions() throws Exception {
        transactions.add(sale(IN_RANGE, "10.00").withStatus(TransactionStatus.DECLINED));
        transactions.add(sale(IN_RANGE, "20.00").withStatus(TransactionStatus.DECLINED).withCurrency("USD"));

        assertThat(summary(user, FROM, TO).declinedCount()).isEqualTo(1);
    }

    @Test
    void latestTransactionsAreEurOnlyWithEveryStatusAndType() throws Exception {
        transactions.add(sale(IN_RANGE, "10.00").withType(TransactionType.REFUND));
        transactions.add(sale(IN_RANGE, "20.00").withStatus(TransactionStatus.DECLINED));
        transactions.add(sale(IN_RANGE, "30.00").withStatus(TransactionStatus.CANCELLED)
                .withType(TransactionType.STORNO));
        transactions.add(sale(IN_RANGE, "40.00").withCurrency("USD"));

        assertThat(summary(user, FROM, TO).latestTransactions())
                .extracting(LatestTransactionResponse::status)
                .containsExactlyInAnyOrder("APPROVED", "DECLINED", "CANCELLED");
    }

    private void addCountedRowsAcrossTheRange() {
        transactions.add(sale(FROM.atStartOfDay(ZAGREB).toInstant(), "11.11"));
        transactions.add(sale(TO.atTime(23, 59, 59).atZone(ZAGREB).toInstant(), "22.22")
                .withType(TransactionType.SALE_WITH_INSTALLMENTS));
        transactions.add(sale(noonOf(LocalDate.of(2026, 10, 24)), "33.33"));
        transactions.add(sale(noonOf(LocalDate.of(2026, 10, 26)), "44.44"));
    }

    private void addRowsThatDoNotCount() {
        transactions.add(sale(FROM.minusDays(1).atTime(23, 59, 59).atZone(ZAGREB).toInstant(), "500.00"));
        transactions.add(sale(TO.plusDays(1).atStartOfDay(ZAGREB).toInstant(), "600.00"));
        transactions.add(sale(IN_RANGE, "700.00").withCurrency("USD"));
        transactions.add(sale(IN_RANGE, "800.00").withType(TransactionType.REFUND));
        transactions.add(sale(IN_RANGE, "900.00").withType(TransactionType.STORNO));
        transactions.add(sale(IN_RANGE, "950.00").withType(TransactionType.TECHNICAL_REVERSAL));
        transactions.add(sale(IN_RANGE, "975.00").withStatus(TransactionStatus.CANCELLED));
        transactions.add(sale(IN_RANGE, "1.00").withStatus(TransactionStatus.DECLINED));
        transactions.add(sale(IN_RANGE, "2.00").withStatus(TransactionStatus.DECLINED));
        transactions.add(sale(IN_RANGE, "3.00").withStatus(TransactionStatus.DECLINED).withCurrency("USD"));
    }

    private TestTransaction sale(Instant at, String amount) {
        return TestTransaction.approvedEurSale(terminal, acquirer, at, amount);
    }

    private DashboardSummaryResponse summary(TestUser viewer, LocalDate from, LocalDate to) throws Exception {
        String body = performAs(viewer, get("/api/dashboard/summary")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, DashboardSummaryResponse.class);
    }

    private List<TransactionResponse> listedEurTransactions(String listedStatus, String... types) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/transactions")
                .param("from", startOf(FROM))
                .param("to", startOf(TO.plusDays(1)))
                .param("status", listedStatus)
                .param("pageSize", "100");
        if (types.length > 0) {
            request.param("type", types);
        }
        String body = performAs(user, request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        PageResponse<TransactionResponse> page = jsonMapper.readValue(body, new TypeReference<>() {
        });
        assertThat(page.items()).hasSize((int) page.total());
        return page.items().stream().filter(item -> "EUR".equals(item.currency())).toList();
    }

    private static void expectTotals(DashboardSummaryResponse summary, String amount, long count) {
        assertThat(summary.totalAmount()).isEqualByComparingTo(amount);
        assertThat(summary.totalCount()).isEqualTo(count);
    }

    private static TrendPointResponse trendPoint(DashboardSummaryResponse summary, LocalDateTime start) {
        return summary.trend().points().stream()
                .filter(point -> point.start().equals(start))
                .findFirst().orElseThrow();
    }

    private static <T> BigDecimal sum(List<T> rows, Function<T, BigDecimal> amount) {
        return rows.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Instant noonOf(LocalDate day) {
        return day.atTime(12, 0).atZone(ZAGREB).toInstant();
    }

    private static String startOf(LocalDate day) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(day.atStartOfDay(ZAGREB));
    }
}
