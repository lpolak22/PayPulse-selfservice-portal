package com.example.paypulse.repository;

import com.example.paypulse.dto.outlet.OutletSearchCriteria;
import com.example.paypulse.model.Outlet;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OutletSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private OutletSpecifications() {
    }

    public static Specification<Outlet> matching(Specification<Outlet> scope, Long merchantId,
                                                 OutletSearchCriteria criteria) {
        List<Specification<Outlet>> specifications = new ArrayList<>();
        specifications.add(scope);
        specifications.add(belongsTo(merchantId));
        if (criteria.hasSearch()) {
            specifications.add(nameOrOutletIdMatches(criteria.search().strip()));
        }
        if (criteria.hasCity()) {
            specifications.add(inCity(criteria.city().strip()));
        }
        if (criteria.hasStatus()) {
            specifications.add(hasStatus(criteria.status().strip()));
        }
        return Specification.allOf(specifications);
    }

    public static Specification<Outlet> belongsTo(Long merchantId) {
        return (root, query, builder) -> builder.equal(root.get("merchant").get("idMerchant"), merchantId);
    }

    static Specification<Outlet> inCity(String city) {
        return (root, query, builder) ->
                builder.equal(builder.lower(builder.trim(root.get("city"))), city.toLowerCase(Locale.ROOT));
    }

    static Specification<Outlet> hasStatus(String status) {
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("status")), status.toLowerCase(Locale.ROOT));
    }

    static Specification<Outlet> nameOrOutletIdMatches(String search) {
        String lower = search.toLowerCase(Locale.ROOT);
        String pattern = "%" + escapeLikeWildcards(lower) + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("name")), pattern, LIKE_ESCAPE),
                builder.equal(builder.lower(root.get("sourceId")), lower));
    }

    private static String escapeLikeWildcards(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
