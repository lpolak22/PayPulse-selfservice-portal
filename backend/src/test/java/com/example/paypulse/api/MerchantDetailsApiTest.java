package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.MerchantFixtures;
import com.example.paypulse.support.TerminalFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MerchantDetailsApiTest extends ApiIntegrationTest {

    private MerchantFixtures merchants;
    private TerminalFixtures terminals;
    private long organisationId;
    private TestUser user;

    @BeforeEach
    void setUp() {
        merchants = new MerchantFixtures(jdbc);
        terminals = new TerminalFixtures(jdbc);
        organisationId = fixtures.organisation();
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void ownMerchantIsReturnedWithItsOutletsAndTerminals() throws Exception {
        String oib = MerchantFixtures.oib();
        String sourceId = MerchantFixtures.sourceId();
        long merchantId = merchants.merchant(organisationId, "Konzum", oib, sourceId, "ACTIVE");
        long outletId = merchants.outlet(merchantId, "Konzum Ilica", MerchantFixtures.sourceId(), "Zagreb", "HOLD");
        String tid = "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long terminalId = terminals.terminal(outletId, tid, "POS", "ACTIVE");

        performAs(user, get(url(merchantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(merchantId))
                .andExpect(jsonPath("$.merchantId").value(sourceId))
                .andExpect(jsonPath("$.name").value("Konzum"))
                .andExpect(jsonPath("$.registrationNumber").value(oib))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.outlets.total").value(1))
                .andExpect(jsonPath("$.outlets.href").value(url(merchantId) + "/outlets"))
                .andExpect(jsonPath("$.outlets.items[0].id").value(outletId))
                .andExpect(jsonPath("$.outlets.items[0].name").value("Konzum Ilica"))
                .andExpect(jsonPath("$.outlets.items[0].city").value("Zagreb"))
                .andExpect(jsonPath("$.outlets.items[0].status").value("HOLD"))
                .andExpect(jsonPath("$.terminals.total").value(1))
                .andExpect(jsonPath("$.terminals.href").value("/api/terminals?merchantId=" + merchantId))
                .andExpect(jsonPath("$.terminals.items[0].id").value(terminalId))
                .andExpect(jsonPath("$.terminals.items[0].terminalId").value(tid))
                .andExpect(jsonPath("$.terminals.items[0].outlet.id").value(outletId))
                .andExpect(jsonPath("$.terminals.items[0].outlet.name").value("Konzum Ilica"))
                .andExpect(jsonPath("$.terminals.items[0].status").value("ACTIVE"));
    }

    @Test
    void merchantOfAnotherOrganisationReturnsNotFound() throws Exception {
        long otherMerchant = merchants.merchant(fixtures.organisation(), "Strani");

        performAs(user, get(url(otherMerchant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.name").doesNotExist());
    }

    @Test
    void missingMerchantReturnsNotFound() throws Exception {
        performAs(user, get(url(Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private static String url(long merchantId) {
        return "/api/merchants/" + merchantId;
    }
}
