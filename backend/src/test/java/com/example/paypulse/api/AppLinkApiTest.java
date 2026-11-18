package com.example.paypulse.api;

import com.example.paypulse.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppLinkApiTest extends ApiIntegrationTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig-_x";

    @Test
    void theActivationLinkIsPublicAndHandsTheTokenToTheApp() throws Exception {
        mockMvc.perform(get("/open/activate").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("href=\"paypulse://activate?token=" + TOKEN + "\"")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    void thePasswordResetLinkOpensTheResetScreen() throws Exception {
        mockMvc.perform(get("/open/reset-password").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"paypulse://reset-password?token=" + TOKEN + "\"")));
    }

    @Test
    void aStaleAuthorizationHeaderIsIgnored() throws Exception {
        mockMvc.perform(get("/open/activate").param("token", TOKEN).header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isOk());
    }

    @Test
    void aMissingOrMalformedTokenShowsAnInvalidLinkPage() throws Exception {
        mockMvc.perform(get("/open/activate"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("This link is not valid.")));
        mockMvc.perform(get("/open/activate").param("token", "<script>alert(1)</script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("paypulse://"))));
    }

    @Test
    void anUnknownTargetIsNotFound() throws Exception {
        mockMvc.perform(get("/open/login").param("token", TOKEN))
                .andExpect(status().isNotFound());
    }
}
