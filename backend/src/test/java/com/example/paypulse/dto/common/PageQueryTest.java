package com.example.paypulse.dto.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageQueryTest {

    @Test
    void pageSizeAbove100IsCappedAt100() {
        PageQuery query = new PageQuery(0, 500);

        assertThat(query.pageSize()).isEqualTo(100);
    }

    @Test
    void missingValuesUseDefaults() {
        PageQuery query = new PageQuery(null, null);

        assertThat(query.page()).isZero();
        assertThat(query.pageSize()).isEqualTo(PageQuery.DEFAULT_PAGE_SIZE);
    }

    @Test
    void negativeOrZeroValuesUseDefaults() {
        PageQuery query = new PageQuery(-3, 0);

        assertThat(query.page()).isZero();
        assertThat(query.pageSize()).isEqualTo(PageQuery.DEFAULT_PAGE_SIZE);
    }

    @Test
    void toPageableUsesCappedValues() {
        Pageable pageable = new PageQuery(2, 1000).toPageable();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void pageResponseMapsSpringPage() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 7);

        PageResponse<Integer> response = PageResponse.from(page, String::length);

        assertThat(response.items()).containsExactly(1, 1);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(7);
    }
}
