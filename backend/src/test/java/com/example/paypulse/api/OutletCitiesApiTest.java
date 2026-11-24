package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.MerchantFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OutletCitiesApiTest extends ApiIntegrationTest {

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
    void eachCityIsListedOnceSortedByName() throws Exception {
        outlet(merchantId, "Zagreb");
        outlet(merchantId, "Split");
        outlet(merchantId, "Zagreb");
        outlet(merchantId, "Osijek");

        assertThat(cities(merchantId)).containsExactly("Osijek", "Split", "Zagreb");
    }

    @Test
    void sameCityWithDifferentCaseOrSpacesIsListedOnce() throws Exception {
        outlet(merchantId, "Zagreb ");
        outlet(merchantId, "ZAGREB");
        outlet(merchantId, "Zagreb");
        outlet(merchantId, " Split");

        assertThat(cities(merchantId)).containsExactly("Split", "Zagreb");
    }

    @Test
    void citiesAreSortedInCroatianAlphabeticalOrder() throws Exception {
        outlet(merchantId, "Dubrovnik");
        outlet(merchantId, "Čakovec");
        outlet(merchantId, "Cres");
        outlet(merchantId, "Šibenik");
        outlet(merchantId, "Split");

        assertThat(cities(merchantId)).containsExactly("Cres", "Čakovec", "Dubrovnik", "Split", "Šibenik");
    }

    @Test
    void outletsWithoutACityAreLeftOut() throws Exception {
        outlet(merchantId, "Rijeka");
        outlet(merchantId, null);
        outlet(merchantId, "   ");

        assertThat(cities(merchantId)).containsExactly("Rijeka");
    }

    @Test
    void citiesOfOtherMerchantsAreNotListed() throws Exception {
        outlet(merchantId, "Rijeka");
        outlet(merchants.merchant(organisationId, "Pekara"), "Pula");

        assertThat(cities(merchantId)).containsExactly("Rijeka");
    }

    @Test
    void merchantWithoutOutletsHasNoCities() throws Exception {
        assertThat(cities(merchantId)).isEmpty();
    }

    @Test
    void merchantOfAnotherOrganisationReturnsNotFound() throws Exception {
        long otherMerchant = merchants.merchant(fixtures.organisation(), "Strani");
        outlet(otherMerchant, "Zadar");

        performAs(user, get(url(otherMerchant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(url(merchantId)))
                .andExpect(status().isUnauthorized());
    }

    private void outlet(long merchant, String city) {
        merchants.outlet(merchant, "Outlet", "MID-" + UUID.randomUUID().toString().substring(0, 12), city,
                "ACTIVE");
    }

    private static String url(long merchant) {
        return "/api/merchants/" + merchant + "/outlets/cities";
    }

    private List<String> cities(long merchant) throws Exception {
        String body = performAs(user, get(url(merchant))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, new TypeReference<>() {
        });
    }
}
