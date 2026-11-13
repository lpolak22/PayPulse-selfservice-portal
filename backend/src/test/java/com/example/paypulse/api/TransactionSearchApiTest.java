package com.example.paypulse.api;

import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.SalesChannel;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.TransactionFixtures;
import com.example.paypulse.support.TransactionFixtures.TestTerminal;
import com.example.paypulse.support.TransactionFixtures.TestTransaction;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.type.TypeReference;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionSearchApiTest extends ApiIntegrationTest {

    private static final String URL = "/api/transactions";
    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 1);
    private static final String RRN = "123456789012";
    private static final String AUTH_CODE = "A1B2C3";
    private static final Pattern FULL_CARD_NUMBER = Pattern.compile("(?<!\\d)\\d{13,19}(?!\\d)");
    private static final Pattern MASKED_PAN = Pattern.compile("\\d{6}\\*{6}\\d{4}");

    private TransactionFixtures transactions;
    private long organisationId;
    private TestUser user;
    private TestTerminal terminal;
    private long acquirer;

    @BeforeEach
    void setUp() {
        transactions = new TransactionFixtures(jdbc);
        organisationId = fixtures.organisation();
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        terminal = transactions.terminalWithDetails(organisationId);
        acquirer = transactions.acquirer();
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void statusFilterReturnsOnlyThatStatus(TransactionStatus wanted) throws Exception {
        Map<TransactionStatus, Long> ids = onePer(TransactionStatus.class, TestTransaction::withStatus);

        assertThat(ids(search().param("status", wanted.name()))).containsExactly(ids.get(wanted));
    }

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    void typeFilterReturnsOnlyThatType(TransactionType wanted) throws Exception {
        Map<TransactionType, Long> ids = onePer(TransactionType.class, TestTransaction::withType);

        assertThat(ids(search().param("type", wanted.name()))).containsExactly(ids.get(wanted));
    }

    @ParameterizedTest
    @EnumSource(CardBrand.class)
    void cardBrandFilterReturnsOnlyThatBrand(CardBrand wanted) throws Exception {
        Map<CardBrand, Long> ids = onePer(CardBrand.class, TestTransaction::withBrand);

        assertThat(ids(search().param("cardBrand", wanted.name()))).containsExactly(ids.get(wanted));
    }

    @ParameterizedTest
    @EnumSource(SalesChannel.class)
    void salesChannelFilterReturnsOnlyThatChannel(SalesChannel wanted) throws Exception {
        Map<SalesChannel, Long> ids = onePer(SalesChannel.class, TestTransaction::withSalesChannel);

        assertThat(ids(search().param("salesChannel", wanted.name()))).containsExactly(ids.get(wanted));
    }

    @Test
    void salesChannelFilterSkipsTransactionsWithoutChannel() throws Exception {
        Map<SalesChannel, Long> withChannel = onePer(SalesChannel.class, TestTransaction::withSalesChannel);
        long withoutChannel = transactions.add(sale(noon(10), "10.00").withSalesChannel(null));
        String[] allChannels = Arrays.stream(SalesChannel.values()).map(Enum::name).toArray(String[]::new);

        assertThat(ids(search().param("salesChannel", allChannels)))
                .containsExactlyInAnyOrderElementsOf(withChannel.values());
        assertThat(ids(search())).contains(withoutChannel);
    }

    @Test
    void outletFilterReturnsOnlyThatOutlet() throws Exception {
        TestTerminal otherOutletTerminal = transactions.terminalWithDetails(organisationId);
        long wanted = transactions.add(sale(noon(10), "10.00"));
        transactions.add(sale(noon(10), "20.00", otherOutletTerminal));

        assertThat(ids(search().param("outletId", String.valueOf(terminal.outletId())))).containsExactly(wanted);
    }

    @Test
    void terminalFilterReturnsOnlyThatTerminal() throws Exception {
        TestTerminal otherTerminal = transactions.terminalWithDetails(organisationId);
        long wanted = transactions.add(sale(noon(10), "10.00"));
        transactions.add(sale(noon(10), "20.00", otherTerminal));

        assertThat(ids(search().param("terminalId", terminal.code()))).containsExactly(wanted);
    }

    @ParameterizedTest
    @CsvSource({"rrn, " + RRN, "authCode, " + AUTH_CODE, "rrn, ' " + RRN + " '", "authCode, ' " + AUTH_CODE + " '"})
    void exactMatchFilterFindsTheTransactionAndTrimsSpaces(String filter, String value) throws Exception {
        long wanted = transactions.add(sale(noon(10), "10.00").withRrn(RRN).withAuthCode(AUTH_CODE));
        transactions.add(sale(noon(10), "20.00").withRrn("999999999999").withAuthCode("Z9Z9Z9"));

        assertThat(ids(search().param(filter, value))).containsExactly(wanted);
    }

    @ParameterizedTest
    @CsvSource({"rrn, 12345678901", "rrn, 23456789012", "rrn, 1234567890123", "rrn, 1234%",
            "authCode, A1B2C", "authCode, 1B2C3", "authCode, a1b2c3", "authCode, A1B2C3D", "authCode, A1B2%"})
    void exactMatchFilterIgnoresPartialAndCaseDifferentValues(String filter, String value) throws Exception {
        transactions.add(sale(noon(10), "10.00").withRrn(RRN).withAuthCode(AUTH_CODE));

        assertThat(ids(search().param(filter, value))).isEmpty();
    }

    @Test
    void blankRrnAndAuthCodeAreIgnored() throws Exception {
        long first = transactions.add(sale(noon(10), "10.00").withRrn(RRN).withAuthCode(AUTH_CODE));
        long second = transactions.add(sale(noon(11), "20.00"));

        assertThat(ids(search().param("rrn", "  ").param("authCode", ""))).containsExactlyInAnyOrder(first, second);
    }

    @ParameterizedTest
    @CsvSource({"status, APPROVED, DECLINED", "type, SALE, REFUND", "cardBrand, VISA, MASTERCARD",
            "salesChannel, POS, ECOM"})
    void valuesInsideOneFilterAreCombinedWithOr(String filter, String first, String second) throws Exception {
        List<Long> ids = addThreeDifferentTransactions();

        assertThat(ids(search().param(filter, first, second))).containsExactlyInAnyOrder(ids.get(0), ids.get(1));
        assertThat(ids(search().param(filter, first + "," + second)))
                .containsExactlyInAnyOrder(ids.get(0), ids.get(1));
    }

    @Test
    void differentFiltersAreCombinedWithAnd() throws Exception {
        long wanted = transactions.add(sale(noon(10), "10.00").withRrn(RRN));
        transactions.add(sale(noon(10), "20.00").withBrand(CardBrand.MASTERCARD).withRrn(RRN));
        transactions.add(sale(noon(10), "30.00").withStatus(TransactionStatus.DECLINED).withRrn(RRN));
        transactions.add(sale(noon(10), "40.00"));

        assertThat(ids(search().param("status", "APPROVED").param("cardBrand", "VISA").param("rrn", RRN)
                .param("terminalId", terminal.code()))).containsExactly(wanted);
    }

    @ParameterizedTest
    @CsvSource({"date, asc, 0 1 2", "date, desc, 2 1 0", "amount, asc, 1 2 0", "amount, desc, 0 2 1",
            "DATE, DESC, 2 1 0", "Amount, Asc, 1 2 0"})
    void sortsByEveryFieldInBothDirections(String sort, String direction, String expectedOrder) throws Exception {
        List<Long> ids = List.of(
                transactions.add(sale(noon(3), "30.00")),
                transactions.add(sale(noon(5), "10.00")),
                transactions.add(sale(noon(7), "20.00")));
        List<Long> expected = Arrays.stream(expectedOrder.split(" ")).map(i -> ids.get(Integer.parseInt(i))).toList();

        assertThat(ids(search().param("sort", sort).param("direction", direction))).containsExactlyElementsOf(expected);
    }

    @ParameterizedTest
    @CsvSource({"date, asc", "date, desc", "amount, asc", "amount, desc"})
    void equalSortValuesAreOrderedById(String sort, String direction) throws Exception {
        long first = transactions.add(sale(noon(10), "10.00"));
        long second = transactions.add(sale(noon(10), "10.00"));
        List<Long> expected = direction.equals("asc") ? List.of(first, second) : List.of(second, first);

        assertThat(ids(search().param("sort", sort).param("direction", direction))).containsExactlyElementsOf(expected);
    }

    @Test
    void defaultSortIsNewestFirst() throws Exception {
        long older = transactions.add(sale(noon(3), "99.00"));
        long newer = transactions.add(sale(noon(7), "1.00"));

        assertThat(ids(search())).containsExactly(newer, older);
    }

    @ParameterizedTest
    @CsvSource({"sort, merchant", "sort, outlet", "direction, up", "direction, ascending"})
    void unknownSortOrDirectionIsBadRequest(String param, String value) throws Exception {
        expectError(search().param(param, value), 400, "BAD_REQUEST");
    }

    @Test
    void pagingReturnsRequestedSliceAndTotal() throws Exception {
        List<Long> ids = addSales(5);

        PageResponse<TransactionResponse> page = page(search().param("page", "1").param("pageSize", "2"));

        assertThat(page.items()).extracting(TransactionResponse::id).containsExactly(ids.get(2), ids.get(1));
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.pageSize()).isEqualTo(2);
        assertThat(page.total()).isEqualTo(5);
    }

    @Test
    void pageAfterLastReturnsEmptyItemsWithTotal() throws Exception {
        addSales(5);

        PageResponse<TransactionResponse> page = page(search().param("page", "10").param("pageSize", "2"));

        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isEqualTo(10);
        assertThat(page.total()).isEqualTo(5);
    }

    @Test
    void pageSizeAbove100IsCappedTo100() throws Exception {
        addSales(101);

        PageResponse<TransactionResponse> page = page(search().param("pageSize", "500"));

        assertThat(page.items()).hasSize(100);
        assertThat(page.pageSize()).isEqualTo(100);
        assertThat(page.total()).isEqualTo(101);
    }

    @Test
    void defaultPageSizeIs20() throws Exception {
        addSales(21);

        PageResponse<TransactionResponse> page = page(search());

        assertThat(page.items()).hasSize(20);
        assertThat(page.pageSize()).isEqualTo(20);
        assertThat(page.total()).isEqualTo(21);
    }

    @Test
    void withoutFromAndToReturnsLastSevenDays() throws Exception {
        Instant now = Instant.now();
        long recent = transactions.add(sale(now.minus(Duration.ofDays(6)), "10.00"));
        transactions.add(sale(now.minus(Duration.ofDays(8)), "20.00"));
        transactions.add(sale(now.plus(Duration.ofHours(1)), "30.00"));

        assertThat(ids(get(URL))).containsExactly(recent);
    }

    @Test
    void fromIsInclusiveAndToIsExclusive() throws Exception {
        long atFrom = transactions.add(sale(startOf(FROM).toInstant(), "10.00"));
        transactions.add(sale(startOf(TO).toInstant(), "20.00"));
        transactions.add(sale(startOf(FROM).toInstant().minusSeconds(1), "30.00"));

        assertThat(ids(search())).containsExactly(atFrom);
    }

    @Test
    void exactlyThirteenMonthsIsAllowed() throws Exception {
        ZonedDateTime from = startOf(LocalDate.of(2025, 10, 1));

        performAs(user, range(from, from.plusMonths(13))).andExpect(status().isOk());
    }

    @Test
    void thirteenMonthsAndOneDayIsRangeTooLarge() throws Exception {
        ZonedDateTime from = startOf(LocalDate.of(2025, 10, 1));

        expectError(range(from, from.plusMonths(13).plusDays(1)), 400, "RANGE_TOO_LARGE");
    }

    @Test
    void fromAfterToIsBadRequest() throws Exception {
        expectError(range(startOf(TO), startOf(FROM)), 400, "BAD_REQUEST");
    }

    @Test
    void equalFromAndToReturnsEmptyPage() throws Exception {
        transactions.add(sale(startOf(FROM).toInstant(), "10.00"));

        assertThat(page(range(startOf(FROM), startOf(FROM))).total()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"from, 2026-10-01T00:00:00", "to, 2026-11-01T00:00:00", "from, 2026-10-01", "from, yesterday"})
    void dateWithoutOffsetIsBadRequest(String param, String value) throws Exception {
        expectError(get(URL).param(param, value), 400, "BAD_REQUEST");
    }

    @ParameterizedTest
    @CsvSource({"status, PENDING", "type, VOID", "cardBrand, VISAX", "salesChannel, ATM", "outletId, abc"})
    void unknownFilterValueIsBadRequest(String param, String value) throws Exception {
        expectError(search().param(param, value), 400, "BAD_REQUEST");
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(search())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void adminAndUserBothGetTransactions(UserRole role) throws Exception {
        TestUser viewer = fixtures.user(organisationId, role, UserStatus.ACTIVE);
        long id = transactions.add(sale(noon(10), "10.00"));

        assertThat(page(viewer, search()).items()).extracting(TransactionResponse::id).containsExactly(id);
    }

    @Test
    void transactionsOfAnotherOrganisationAreNeverReturned() throws Exception {
        long otherOrganisation = fixtures.organisation();
        TestUser otherAdmin = fixtures.user(otherOrganisation, UserRole.ADMIN, UserStatus.ACTIVE);
        TestTerminal foreignTerminal = transactions.terminalWithDetails(otherOrganisation);
        long own = transactions.add(sale(noon(10), "10.00").withRrn(RRN).withAuthCode(AUTH_CODE));
        long foreign = transactions.add(sale(noon(10), "20.00", foreignTerminal).withRrn(RRN).withAuthCode(AUTH_CODE));

        assertThat(ids(search())).containsExactly(own);
        assertThat(ids(search().param("rrn", RRN))).containsExactly(own);
        assertThat(ids(search().param("authCode", AUTH_CODE))).containsExactly(own);
        assertThat(page(otherAdmin, search()).items()).extracting(TransactionResponse::id).containsExactly(foreign);
    }

    @Test
    void foreignOutletIdReturnsEmptyPage() throws Exception {
        TestTerminal foreignTerminal = transactions.terminalWithDetails(fixtures.organisation());
        transactions.add(sale(noon(10), "20.00", foreignTerminal));

        String foreignOutletId = String.valueOf(foreignTerminal.outletId());

        PageResponse<TransactionResponse> page = page(search().param("outletId", foreignOutletId));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @Test
    void foreignTerminalIdReturnsEmptyPage() throws Exception {
        TestTerminal foreignTerminal = transactions.terminalWithDetails(fixtures.organisation());
        transactions.add(sale(noon(10), "20.00", foreignTerminal));

        PageResponse<TransactionResponse> page = page(search().param("terminalId", foreignTerminal.code()));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @Test
    void responsesNeverContainFullCardNumberOrRawData() throws Exception {
        transactions.add(sale(noon(10), "10.00").withRrn(RRN).withAuthCode(AUTH_CODE));
        transactions.add(sale(noon(11), "20.00").withBrand(CardBrand.MASTERCARD).withMaskedPan("535522******4321"));
        transactions.add(sale(noon(12), "30.00").withSalesChannel(null));

        for (MockHttpServletRequestBuilder request : List.of(search(), search().param("rrn", RRN), get(URL))) {
            String body = performAs(user, request).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(body).doesNotContainPattern(FULL_CARD_NUMBER)
                    .doesNotContain("raw_data")
                    .doesNotContain("rawData");
        }
        assertThat(page(search()).items()).hasSize(3)
                .allSatisfy(item -> assertThat(item.maskedPan()).matches(MASKED_PAN));
    }

    private <E extends Enum<E>> Map<E, Long> onePer(Class<E> type,
                                                     BiFunction<TestTransaction, E, TestTransaction> variant) {
        Map<E, Long> ids = new EnumMap<>(type);
        for (E value : type.getEnumConstants()) {
            ids.put(value, transactions.add(variant.apply(sale(noon(10), "10.00"), value)));
        }
        return ids;
    }

    private List<Long> addThreeDifferentTransactions() {
        return List.of(
                transactions.add(sale(noon(10), "10.00")),
                transactions.add(sale(noon(10), "20.00").withStatus(TransactionStatus.DECLINED)
                        .withType(TransactionType.REFUND).withBrand(CardBrand.MASTERCARD)
                        .withSalesChannel(SalesChannel.ECOM)),
                transactions.add(sale(noon(10), "30.00").withStatus(TransactionStatus.CANCELLED)
                        .withType(TransactionType.STORNO).withBrand(CardBrand.AMEX)
                        .withSalesChannel(SalesChannel.MOTO)));
    }

    private List<Long> addSales(int count) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(transactions.add(sale(startOf(FROM).plusMinutes(i).toInstant(), "10.00")));
        }
        return ids;
    }

    private TestTransaction sale(Instant at, String amount) {
        return sale(at, amount, terminal);
    }

    private TestTransaction sale(Instant at, String amount, TestTerminal onTerminal) {
        return TestTransaction.approvedEurSale(onTerminal.id(), acquirer, at, amount);
    }

    private MockHttpServletRequestBuilder search() {
        return range(startOf(FROM), startOf(TO));
    }

    private static MockHttpServletRequestBuilder range(ZonedDateTime from, ZonedDateTime to) {
        return get(URL).param("from", iso(from)).param("to", iso(to));
    }

    private List<Long> ids(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(TransactionResponse::id).toList();
    }

    private PageResponse<TransactionResponse> page(MockHttpServletRequestBuilder request) throws Exception {
        return page(user, request);
    }

    private PageResponse<TransactionResponse> page(TestUser viewer, MockHttpServletRequestBuilder request)
            throws Exception {
        String body = performAs(viewer, request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, new TypeReference<>() {
        });
    }

    private void expectError(MockHttpServletRequestBuilder request, int httpStatus, String code) throws Exception {
        performAs(user, request)
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code));
    }

    private static Instant noon(int dayOfOctober) {
        return LocalDate.of(2026, 10, dayOfOctober).atTime(12, 0).atZone(ZAGREB).toInstant();
    }

    private static ZonedDateTime startOf(LocalDate day) {
        return day.atStartOfDay(ZAGREB);
    }

    private static String iso(ZonedDateTime time) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time);
    }
}
