package com.example.paypulse.dto.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination parameters read from the query string, e.g. {@code ?page=0&pageSize=20}.
 * Pages are zero-based. pageSize defaults to 20 and is capped at 100.
 */
public record PageQuery(Integer page, Integer pageSize) {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        page = (page == null || page < 0) ? 0 : page;
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public Pageable toPageable() {
        return PageRequest.of(page, pageSize);
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page, pageSize, sort);
    }
}
