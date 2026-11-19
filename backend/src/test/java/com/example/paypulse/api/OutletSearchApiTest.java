package com.example.paypulse.api;

import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.outlet.OutletResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OutletSearchApiTest extends ApiIntegrationTest {

    private MerchantFixtures merchants;
    private long organisationId;
    private long merchantId;
    private TestUser user;

    @BeforeEach
    void setUp() {
        merchants = new MerchantFixtures(jdbc);
        organisationId = fixtures.organisation();
        merchantId = merchants.merchant(organisationId, "Konzum");
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void cityFilterReturnsOnlyOutletsInThatCity() throws Exception {
        for (int i = 0; i < 8; i++) {
            outlet("Zagreb " + i, "Zagreb");
        }
        for (int i = 0; i < 4; i++) {
            outlet("Split " + i, "Split");
        }

        PageResponse<OutletResponse> page = page(outlets().param("city", "Split"));

        assertThat(page.total()).isEqualTo(4);
        assertThat(page.items()).extracting(OutletResponse::city).containsOnly("Split");
    }

    @Test
    void onlyOutletsOfTheRequestedMerchantAreReturned() throws Exception {
        long own = outlet("Konzum Ilica", "Zagreb");
        long otherMerchant = merchants.merchant(organisationId, "Pekara");
        merchants.outlet(otherMerchant, "Pekara Ilica", sourceId(), "Zagreb", "ACTIVE");

        assertThat(ids(outlets())).containsExactly(own);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ilica", "ILICA", "nzum ili"})
    void nameSearchIgnoresCaseAndMatchesPartialNames(String search) throws Exception {
        long ilica = outlet("Konzum Ilica", "Zagreb");
        outlet("Konzum Riva", "Split");

        assertThat(ids(outlets().param("search", search))).containsExactly(ilica);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-ZG-001", "mid-zg-001", " MID-ZG-001 "})
    void outletIdSearchMatchesTheWholeIdIgnoringCase(String search) throws Exception {
        long wanted = merchants.outlet(merchantId, "Konzum Ilica", "MID-ZG-001", "Zagreb", "ACTIVE");
        merchants.outlet(merchantId, "Konzum Riva", "MID-ST-001", "Split", "ACTIVE");

        assertThat(ids(outlets().param("search", search))).containsExactly(wanted);
    }

    @Test
    void outletIdSearchDoesNotMatchPartOfTheId() throws Exception {
        merchants.outlet(merchantId, "Konzum Ilica", "MID-ZG-001", "Zagreb", "ACTIVE");

        assertThat(ids(outlets().param("search", "MID-ZG"))).isEmpty();
    }

    @Test
    void cityFilterWorksTogetherWithSearch() throws Exception {
        long splitKonzum = outlet("Konzum Riva", "Split");
        outlet("Konzum Ilica", "Zagreb");
        outlet("Pekara Riva", "Split");

        assertThat(ids(outlets().param("search", "konzum").param("city", "Split"))).containsExactly(splitKonzum);
    }

    @Test
    void statusFilterIgnoresCase() throws Exception {
        long onHold = merchants.outlet(merchantId, "Konzum Riva", sourceId(), "Split", "HOLD");
        outlet("Konzum Ilica", "Zagreb");

        assertThat(ids(outlets().param("status", "hold"))).containsExactly(onHold);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO", "ACTVE", "UNKNOWN"})
    void unknownStatusIsValidationError(String status) throws Exception {
        performAs(user, outlets().param("status", status))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Zagreb", "ZAGREB", "zagreb", " Zagreb "})
    void cityFilterIgnoresCaseAndSurroundingSpaces(String city) throws Exception {
        long padded = outlet("Konzum Ilica", "Zagreb ");
        long upper = outlet("Konzum Dubrava", "ZAGREB");
        long plain = outlet("Konzum Trešnjevka", "Zagreb");
        outlet("Konzum Riva", "Split");

        assertThat(ids(outlets().param("city", city))).containsExactlyInAnyOrder(padded, upper, plain);
    }

    @Test
    void likeWildcardsInSearchAreTreatedAsText() throws Exception {
        outlet("Konzum Ilica", "Zagreb");

        assertThat(ids(outlets().param("search", "%"))).isEmpty();
    }

    @Test
    void noMatchReturnsAnEmptyPage() throws Exception {
        outlet("Konzum Ilica", "Zagreb");

        PageResponse<OutletResponse> page = page(outlets().param("city", "Rijeka"));

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @Test
    void resultsArePaginatedAndSortedByName() throws Exception {
        for (int i = 11; i >= 0; i--) {
            outlet(String.format("Outlet %02d", i), "Zagreb");
        }

        PageResponse<OutletResponse> first = page(outlets().param("pageSize", "5"));
        PageResponse<OutletResponse> last = page(outlets().param("pageSize", "5").param("page", "2"));

        assertThat(first.total()).isEqualTo(12);
        assertThat(first.items()).extracting(OutletResponse::name)
                .containsExactly("Outlet 00", "Outlet 01", "Outlet 02", "Outlet 03", "Outlet 04");
        assertThat(last.items()).extracting(OutletResponse::name).containsExactly("Outlet 10", "Outlet 11");
    }

    @Test
    void eachOutletShowsItsDataAndTerminalCount() throws Exception {
        long outlet = merchants.outlet(merchantId, "Konzum Ilica", "MID-ZG-001", "Zagreb", "ACTIVE");
        merchants.terminal(outlet);
        merchants.terminal(outlet);
        outlet("Konzum Riva", "Split");

        assertThat(page(outlets().param("search", "MID-ZG-001")).items()).singleElement()
                .isEqualTo(new OutletResponse(outlet, "MID-ZG-001", "Konzum Ilica", "Ulica 1", "Zagreb", "10000",
                        "ACTIVE", 2));
    }

    @Test
    void merchantOfAnotherOrganisationReturnsNotFound() throws Exception {
        long otherMerchant = merchants.merchant(fixtures.organisation(), "Strani");
        merchants.outlet(otherMerchant, "Strani outlet", sourceId(), "Zagreb", "ACTIVE");

        performAs(user, get(url(otherMerchant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void unknownMerchantReturnsNotFound() throws Exception {
        performAs(user, get(url(Long.MAX_VALUE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(url(merchantId)))
                .andExpect(status().isUnauthorized());
    }

    private long outlet(String name, String city) {
        return merchants.outlet(merchantId, name, sourceId(), city, "ACTIVE");
    }

    private MockHttpServletRequestBuilder outlets() {
        return get(url(merchantId));
    }

    private static String url(long merchant) {
        return "/api/merchants/" + merchant + "/outlets";
    }

    private static String sourceId() {
        return "MID-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private List<Long> ids(MockHttpServletRequestBuilder request) throws Exception {
        return page(request).items().stream().map(OutletResponse::id).toList();
    }

    private PageResponse<OutletResponse> page(MockHttpServletRequestBuilder request) throws Exception {
        String body = performAs(user, request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, new TypeReference<>() {
        });
    }
}
