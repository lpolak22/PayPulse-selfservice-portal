package com.example.paypulse.repository;

import com.example.paypulse.dto.merchant.MerchantSearchCriteria;
import com.example.paypulse.model.Merchant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MerchantSpecifications {

    private static final char LIKE_ESCAPE = '\\';
    private static final String ID = "^\\d{1,18}$";

    private MerchantSpecifications() {
    }

    public static Specification<Merchant> matching(Specification<Merchant> scope, MerchantSearchCriteria criteria) {
        List<Specification<Merchant>> specifications = new ArrayList<>();
        specifications.add(scope);
        if (criteria.hasSearch()) {
            specifications.add(nameIdOrOibMatches(criteria.search().strip()));
        }
        if (criteria.hasOib()) {
            specifications.add(hasOib(criteria.oib().strip()));
        }
        if (criteria.hasStatus()) {
            specifications.add(hasStatus(criteria.status().strip()));
        }
        return Specification.allOf(specifications);
    }

    static Specification<Merchant> hasOib(String oib) {
        return (root, query, builder) -> builder.equal(root.get("registrationNumber"), oib);
    }

    static Specification<Merchant> hasStatus(String status) {
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("status")), status.toLowerCase(Locale.ROOT));
    }

    static Specification<Merchant> nameIdOrOibMatches(String search) {
        String pattern = "%" + escapeLikeWildcards(search.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.like(builder.lower(root.get("name")), pattern, LIKE_ESCAPE));
            predicates.add(builder.equal(root.get("registrationNumber"), search));
            if (search.matches(ID)) {
                predicates.add(builder.equal(root.get("idMerchant"), Long.parseLong(search)));
            }
            return builder.or(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLikeWildcards(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
