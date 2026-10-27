package com.example.paypulse.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standard paginated response body.
 *
 * @param items    items on the current page
 * @param page     zero-based page index
 * @param pageSize requested page size
 * @param total    total number of items across all pages
 */
public record PageResponse<T>(List<T> items, int page, int pageSize, long total) {

    public PageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return from(page.map(mapper));
    }
}
