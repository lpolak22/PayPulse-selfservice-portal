package com.example.paypulse.repository;

import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AppUserSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private AppUserSpecifications() {
    }

    /**
     * Undeleted users of one organisation that match every filter set in the criteria.
     */
    public static Specification<AppUser> matching(Long organisationId, AppUserSearchCriteria criteria) {
        List<Specification<AppUser>> specifications = new ArrayList<>();
        specifications.add(belongsToOrganisation(organisationId));
        specifications.add(isNotDeleted());
        if (criteria.hasSearch()) {
            specifications.add(nameOrEmailContains(criteria.search()));
        }
        if (criteria.role() != null) {
            specifications.add(hasRole(criteria.role()));
        }
        if (criteria.status() != null) {
            specifications.add(hasStatus(criteria.status()));
        }
        return Specification.allOf(specifications);
    }

    static Specification<AppUser> belongsToOrganisation(Long organisationId) {
        return (root, query, builder) ->
                builder.equal(root.get("organisation").get("idOrganisation"), organisationId);
    }

    static Specification<AppUser> isNotDeleted() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    static Specification<AppUser> hasRole(UserRole role) {
        return (root, query, builder) -> builder.equal(root.get("role"), role);
    }

    static Specification<AppUser> hasStatus(UserStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    /**
     * Case-insensitive "contains" match on "first last" name or email.
     */
    static Specification<AppUser> nameOrEmailContains(String search) {
        String pattern = "%" + escapeLikeWildcards(search.strip().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> {
            Expression<String> fullName = builder.concat(
                    builder.concat(root.get("firstName"), " "), root.get("lastName"));
            return builder.or(
                    builder.like(builder.lower(fullName), pattern, LIKE_ESCAPE),
                    builder.like(builder.lower(root.get("email")), pattern, LIKE_ESCAPE));
        };
    }

    private static String escapeLikeWildcards(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
