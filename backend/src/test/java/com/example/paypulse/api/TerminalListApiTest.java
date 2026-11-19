package com.example.paypulse.api;

import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.terminal.TerminalResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.MerchantFixtures;
import com.example.paypulse.support.TerminalFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TerminalListApiTest extends ApiIntegrationTest {

    private static final String URL = "/api/terminals";

    private MerchantFixtures merchants;
    private TerminalFixtures terminals;
    private long organisationId;
    private long outletId;
    private TestUser user;
    private String prefix;

    @BeforeEach
    void setUp() {
        merchants = new MerchantFixtures(jdbc);
        terminals = new TerminalFixtures(jdbc);
        organisationId = fixtures.organisation();
        outletId = merchants.outlet(merchants.merchant(organisationId, "Konzum"));
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        prefix = "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Test
    void firstPageOfFortyTerminalsHasTwentyFiveItemsAndTotalForty() throws Exception {
        for (int i = 0; i < 40; i++) {
            terminal(String.format("%02d", i), "POS", "ACTIVE");
        }
        long otherOutlet = merchants.outlet(merchants.merchant(fixtures.organisation(), "Strani"));
        terminals.terminal(otherOutlet, prefix + "X", "POS", "ACTIVE");

        PageResponse<TerminalResponse> page = page(get(URL).param("pageSize", "25"));

        assertThat(page.items()).hasSize(25);
        assertThat(page.total()).isEqualTo(40);
        assertThat(page.pageSize()).isEqualTo(25);
    }

    @Test
    void secondPageHasTheRemainingTerminals() throws Exception {
        for (int i = 0; i < 40; i++) {
            terminal(String.format("%02d", i), "POS", "ACTIVE");
        }

        PageResponse<TerminalResponse> page = page(get(URL).param("pageSize", "25").param("page", "1"));

        assertThat(page.items()).extracting(TerminalResponse::terminalId)
                .hasSize(15).first().isEqualTo(prefix + "25");
    }

    @Test
    void terminalsOfAnotherOrganisationAreNeverReturned() throws Exception {
        long own = terminal("01", "POS", "ACTIVE");
        long otherOutlet = merchants.outlet(merchants.merchant(fixtures.organisation(), "Strani"));
        terminals.terminal(otherOutlet, prefix + "02", "POS", "ACTIVE");

        assertThat(ids(get(URL))).containsExactly(own);
        assertThat(ids(get(URL).param("status", "ACTIVE").param("type", "POS"))).containsExactly(own);
    }

    @Test
    void terminalsFromAllMerchantsAndOutletsOfTheOrganisationAreReturned() throws Exception {
        long first = terminal("01", "POS", "ACTIVE");
        long secondOutlet = merchants.outlet(merchants.merchant(organisationId, "Pekara"));
        long second = terminals.terminal(secondOutlet, prefix + "02", "POS", "ACTIVE");

        assertThat(ids(get(URL))).containsExactly(first, second);
    }

    @Test
    void eachItemHasTidTypeStatusOutletAndMerchant() throws Exception {
        long merchantId = merchants.merchant(organisationId, "Pekara Dubravica");
        long outlet = merchants.outlet(merchantId, "Pekara Ilica", MerchantFixtures.sourceId(), "Zagreb", "ACTIVE");
        long id = terminals.terminal(outlet, prefix + "01", "SoftPOS V1", "ACTIVE");

        assertThat(page(get(URL).param("type", "SoftPOS V1")).items()).singleElement()
                .isEqualTo(new TerminalResponse(id, prefix + "01", "SoftPOS V1", "ACTIVE",
                        new TerminalResponse.OutletRef(outlet, "Pekara Ilica"),
                        new TerminalResponse.MerchantRef(merchantId, "Pekara Dubravica")));
    }

    @Test
    void unknownTypeAndStatusFromTheDatabaseAreReturnedAsStored() throws Exception {
        long id = terminal("01", "SoftPOS V3", "SOMETHING_NEW");

        assertThat(page(get(URL)).items()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(id);
            assertThat(item.type()).isEqualTo("SoftPOS V3");
            assertThat(item.status()).isEqualTo("SOMETHING_NEW");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE", "inactive", "Inactive"})
    void statusFilterIgnoresLetterCase(String status) throws Exception {
        long inactive = terminal("01", "POS", "INACTIVE");
        terminal("02", "POS", "ACTIVE");

        assertThat(ids(get(URL).param("status", status))).containsExactly(inactive);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SoftPOS V1", "softpos v1", "SOFTPOS_V1", "softposv1"})
    void typeFilterIgnoresCaseSpacesAndUnderscores(String type) throws Exception {
        long spaced = terminal("01", "SoftPOS V1", "ACTIVE");
        long joined = terminal("02", "SoftPOSV1", "ACTIVE");
        terminal("03", "SoftPOS V2", "ACTIVE");
        terminal("04", "POS", "ACTIVE");

        assertThat(ids(get(URL).param("type", type))).containsExactly(spaced, joined);
    }

    @Test
    void terminalStoredAsUnknownIsFoundByUnknownStatusOnly() throws Exception {
        long unknown = terminal("01", "POS", "UNKNOWN");
        long active = terminal("02", "POS", "ACTIVE");

        assertThat(ids(get(URL).param("status", "unknown"))).containsExactly(unknown);
        assertThat(ids(get(URL).param("status", "ACTIVE"))).containsExactly(active);
    }

    @Test
    void statusAndTypeFiltersCanBeCombined() throws Exception {
        long wanted = terminal("01", "MOBILE", "BLOCKED");
        terminal("02", "MOBILE", "ACTIVE");
        terminal("03", "ECR", "BLOCKED");

        assertThat(ids(get(URL).param("status", "blocked").param("type", "mobile"))).containsExactly(wanted);
    }

    @Test
    void filterWithoutMatchesReturnsAnEmptyPage() throws Exception {
        terminal("01", "POS", "ACTIVE");

        PageResponse<TerminalResponse> page = page(get(URL).param("status", "SUSPENDED"));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTVE", "SOMETHING_NEW", "HOLD"})
    void unknownStatusIsValidationError(String status) throws Exception {
        performAs(user, get(URL).param("status", status))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SoftPOS V3", "SATURN", "card"})
    void unknownTypeIsValidationError(String type) throws Exception {
        performAs(user, get(URL).param("type", type))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("type"));
    }

    @Test
    void unknownStatusAndTypeAreReportedTogether() throws Exception {
        performAs(user, get(URL).param("status", "nope").param("type", "nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    @Test
    void unknownSortIsBadRequest() throws Exception {
        performAs(user, get(URL).param("sort", "mcc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void unknownDirectionIsBadRequest() throws Exception {
        performAs(user, get(URL).param("direction", "up"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void defaultOrderIsByTidAscending() throws Exception {
        terminal("03", "POS", "ACTIVE");
        terminal("01", "POS", "ACTIVE");
        terminal("02", "POS", "ACTIVE");

        assertThat(tids(get(URL))).containsExactly(prefix + "01", prefix + "02", prefix + "03");
    }

    @Test
    void sortByMerchantDescendingThenByTid() throws Exception {
        long alfa = merchants.outlet(merchants.merchant(organisationId, "Alfa"));
        long beta = merchants.outlet(merchants.merchant(organisationId, "Beta"));
        terminals.terminal(alfa, prefix + "01", "POS", "ACTIVE");
        terminals.terminal(beta, prefix + "03", "POS", "ACTIVE");
        terminals.terminal(beta, prefix + "02", "POS", "ACTIVE");

        assertThat(tids(get(URL).param("sort", "merchant").param("direction", "desc")))
                .containsExactly(prefix + "02", prefix + "03", prefix + "01");
    }

    @Test
    void sortByStatus() throws Exception {
        terminal("01", "POS", "INACTIVE");
        terminal("02", "POS", "ACTIVE");
        terminal("03", "POS", "BLOCKED");

        assertThat(page(get(URL).param("sort", "status")).items()).extracting(TerminalResponse::status)
                .containsExactly("ACTIVE", "BLOCKED", "INACTIVE");
    }

    @Test
    void organisationWithoutTerminalsReturnsAnEmptyPage() throws Exception {
        PageResponse<TerminalResponse> page = page(get(URL));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void adminAndUserCanBothListTerminals(UserRole role) throws Exception {
        long id = terminal("01", "POS", "ACTIVE");
        TestUser member = fixtures.user(organisationId, role, UserStatus.ACTIVE);

        performAs(member, get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }

    private long terminal(String suffix, String type, String status) {
        return terminals.terminal(outletId, prefix + suffix, type, status);
    }

    private List<Long> ids(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(TerminalResponse::id).toList();
    }

    private List<String> tids(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(TerminalResponse::terminalId).toList();
    }

    private PageResponse<TerminalResponse> page(MockHttpServletRequestBuilder request) throws Exception {
        String body = performAs(user, request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, new TypeReference<>() {
        });
    }
}
