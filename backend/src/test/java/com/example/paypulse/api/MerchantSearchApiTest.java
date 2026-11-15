package com.example.paypulse.api;

import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.MerchantFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import static com.example.paypulse.support.MerchantFixtures.oib;
import static com.example.paypulse.support.MerchantFixtures.oibStartingWith;
import static com.example.paypulse.support.MerchantFixtures.sourceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MerchantSearchApiTest extends ApiIntegrationTest {

    private static final String URL = "/api/merchants";

    private MerchantFixtures merchants;
    private long organisationId;
    private TestUser user;

    @BeforeEach
    void setUp() {
        merchants = new MerchantFixtures(jdbc);
        organisationId = fixtures.organisation();
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"konzum", "KONZUM", "nzum zag"})
    void nameSearchIgnoresCaseAndMatchesPartialNames(String search) throws Exception {
        long konzum = merchants.merchant(organisationId, "Konzum Zagreb");
        merchants.merchant(organisationId, "Pekara Sunce");

        assertThat(ids(search(search))).containsExactly(konzum);
    }

    @Test
    void oibSearchMatchesTheBeginningOfTheOib() throws Exception {
        long first = merchants.merchant(organisationId, "Prvi", oibStartingWith("012345"), sourceId(), "ACTIVE");
        long second = merchants.merchant(organisationId, "Drugi", oibStartingWith("012345"), sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Treci", oibStartingWith("054321"), sourceId(), "ACTIVE");

        assertThat(ids(search("01234"))).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void oibSearchDoesNotMatchTheMiddleOfTheOib() throws Exception {
        merchants.merchant(organisationId, "Prvi", oibStartingWith("098765"), sourceId(), "ACTIVE");

        assertThat(ids(search("8765"))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SP-ZRG", "sp-zrg", "SP-ZRG-M1"})
    void merchantIdSearchMatchesTheBeginningIgnoringCase(String search) throws Exception {
        long wanted = merchants.merchant(organisationId, "Prvi", oib(), "SP-ZRG-M1", "ACTIVE");
        merchants.merchant(organisationId, "Drugi", oib(), "SP-ABC-M1", "ACTIVE");

        assertThat(ids(search(search))).containsExactly(wanted);
    }

    @Test
    void internalIdIsNotSearchable() throws Exception {
        long merchant = merchants.merchant(organisationId, "Prvi");

        assertThat(ids(search(String.valueOf(merchant)))).isEmpty();
    }

    @Test
    void statusFilterCanBeCombinedWithSearch() throws Exception {
        long active = merchants.merchant(organisationId, "Konzum Zagreb", oib(), sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Konzum Split", oib(), sourceId(), "HOLD");
        merchants.merchant(organisationId, "Pekara Sunce", oib(), sourceId(), "ACTIVE");

        assertThat(ids(search("konzum").param("status", "active"))).containsExactly(active);
    }

    @Test
    void responseHasMerchantIdOutletAndTerminalCounts() throws Exception {
        long merchant = merchants.merchant(organisationId, "Prvi", oib(), "SP-COUNT-1", "ACTIVE");
        long firstOutlet = merchants.outlet(merchant);
        long secondOutlet = merchants.outlet(merchant);
        merchants.terminal(firstOutlet);
        merchants.terminal(firstOutlet);
        merchants.terminal(secondOutlet);

        MerchantResponse response = page(get(URL)).items().getFirst();

        assertThat(response.merchantId()).isEqualTo("SP-COUNT-1");
        assertThat(response.outletCount()).isEqualTo(2);
        assertThat(response.terminalCount()).isEqualTo(3);
    }

    @Test
    void merchantWithoutOutletsHasZeroCounts() throws Exception {
        merchants.merchant(organisationId, "Prvi");

        MerchantResponse response = page(get(URL)).items().getFirst();

        assertThat(response.outletCount()).isZero();
        assertThat(response.terminalCount()).isZero();
    }

    @Test
    void resultsArePaginated() throws Exception {
        merchants.merchant(organisationId, "A");
        merchants.merchant(organisationId, "B");
        merchants.merchant(organisationId, "C");

        PageResponse<MerchantResponse> first = page(get(URL).param("page", "0").param("pageSize", "2"));
        PageResponse<MerchantResponse> second = page(get(URL).param("page", "1").param("pageSize", "2"));

        assertThat(first.items()).extracting(MerchantResponse::name).containsExactly("A", "B");
        assertThat(second.items()).extracting(MerchantResponse::name).containsExactly("C");
        assertThat(first.total()).isEqualTo(3);
    }

    @Test
    void noMatchReturnsAnEmptyPage() throws Exception {
        merchants.merchant(organisationId, "Prvi");

        PageResponse<MerchantResponse> page = page(search("nepostojeci"));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @Test
    void merchantsOfAnotherOrganisationAreNeverReturned() throws Exception {
        long otherOrganisation = fixtures.organisation();
        long own = merchants.merchant(organisationId, "Konzum Zagreb", oibStartingWith("011111"), "SP-OWN-1", "ACTIVE");
        merchants.merchant(otherOrganisation, "Konzum Rijeka", oibStartingWith("011111"), "SP-OWN-2", "ACTIVE");

        assertThat(ids(get(URL))).containsExactly(own);
        assertThat(ids(search("konzum"))).containsExactly(own);
        assertThat(ids(search("01111"))).containsExactly(own);
        assertThat(ids(search("SP-OWN"))).containsExactly(own);
    }

    @Test
    void statusFilterIgnoresLetterCase() throws Exception {
        long hold = merchants.merchant(organisationId, "Prvi", oib(), sourceId(), "HOLD");
        merchants.merchant(organisationId, "Drugi", oib(), sourceId(), "ACTIVE");

        assertThat(ids(get(URL).param("status", "hold"))).containsExactly(hold);
    }

    @Test
    void oibParameterMatchesTheWholeOib() throws Exception {
        String oib = oibStartingWith("022222");
        long wanted = merchants.merchant(organisationId, "Prvi", oib, sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Drugi", oibStartingWith("022222"), sourceId(), "ACTIVE");

        assertThat(ids(get(URL).param("oib", oib))).containsExactly(wanted);
    }

    @Test
    void defaultOrderIsByNameAscending() throws Exception {
        merchants.merchant(organisationId, "Cvjecarna");
        merchants.merchant(organisationId, "Apoteka");
        merchants.merchant(organisationId, "Bistro");

        assertThat(names(get(URL))).containsExactly("Apoteka", "Bistro", "Cvjecarna");
    }

    @Test
    void sortByOibDescending() throws Exception {
        merchants.merchant(organisationId, "Prvi", oibStartingWith("01"), sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Drugi", oibStartingWith("03"), sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Treci", oibStartingWith("02"), sourceId(), "ACTIVE");

        assertThat(names(get(URL).param("sort", "oib").param("direction", "desc")))
                .containsExactly("Drugi", "Treci", "Prvi");
    }

    @Test
    void sortByStatusThenByName() throws Exception {
        merchants.merchant(organisationId, "Zeta", oib(), sourceId(), "ACTIVE");
        merchants.merchant(organisationId, "Alfa", oib(), sourceId(), "HOLD");
        merchants.merchant(organisationId, "Beta", oib(), sourceId(), "ACTIVE");

        assertThat(names(get(URL).param("sort", "STATUS"))).containsExactly("Beta", "Zeta", "Alfa");
    }

    @Test
    void unknownSortIsBadRequest() throws Exception {
        performAs(user, get(URL).param("sort", "city"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("sort must be name, oib or status"));
    }

    @Test
    void unknownDirectionIsBadRequest() throws Exception {
        performAs(user, get(URL).param("direction", "up"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("direction must be asc or desc"));
    }

    @Test
    void unknownStatusIsValidationError() throws Exception {
        performAs(user, get(URL).param("status", "ACTVE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
    }

    @Test
    void invalidOibAndStatusAreReportedTogether() throws Exception {
        performAs(user, get(URL).param("oib", "123").param("status", "ACTVE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("oib"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("status"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private static MockHttpServletRequestBuilder search(String text) {
        return get(URL).param("search", text);
    }

    private List<Long> ids(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(MerchantResponse::id).toList();
    }

    private List<String> names(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(MerchantResponse::name).toList();
    }

    private PageResponse<MerchantResponse> page(MockHttpServletRequestBuilder request) throws Exception {
        String body = performAs(user, request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, new TypeReference<>() {
        });
    }
}
