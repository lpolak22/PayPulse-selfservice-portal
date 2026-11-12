package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "sevenpay.import")
public record SevenPayImportProperties(String organisation, List<String> merchantOibs) {

    public SevenPayImportProperties {
        merchantOibs = merchantOibs == null ? List.of() : List.copyOf(merchantOibs);
    }
}
