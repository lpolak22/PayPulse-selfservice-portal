package com.example.paypulse.integration.sevenpay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SevenPayPage<T>(List<T> content, SevenPayPageInfo page) {

    public List<T> items() {
        return content == null ? List.of() : content;
    }

    public boolean isLast() {
        return page == null || page.number() + 1 >= page.totalPages();
    }
}
