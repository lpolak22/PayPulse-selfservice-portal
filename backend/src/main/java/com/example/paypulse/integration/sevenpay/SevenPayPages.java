package com.example.paypulse.integration.sevenpay;

import com.example.paypulse.integration.sevenpay.dto.SevenPayPage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public final class SevenPayPages {

    private SevenPayPages() {
    }

    public static <T> List<T> fetchAll(IntFunction<SevenPayPage<T>> pageLoader) {
        List<T> items = new ArrayList<>();
        int page = 0;
        SevenPayPage<T> current;
        do {
            current = pageLoader.apply(page++);
            items.addAll(current.items());
        } while (!current.isLast());
        return items;
    }
}
