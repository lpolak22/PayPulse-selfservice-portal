package com.example.paypulse.repository;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class OrganisationScope {

    private static final String ORGANISATION = "organisation";
    private static final String ORGANISATION_ID = "idOrganisation";

    private OrganisationScope() {
    }

    public static Specification<AppUser> users(Long organisationId) {
        return within(organisationId, ORGANISATION);
    }

    public static Specification<Merchant> merchants(Long organisationId) {
        return within(organisationId, ORGANISATION);
    }

    public static Specification<Outlet> outlets(Long organisationId) {
        return within(organisationId, "merchant", ORGANISATION);
    }

    public static Specification<Terminal> terminals(Long organisationId) {
        return within(organisationId, "outlet", "merchant", ORGANISATION);
    }

    public static Specification<Transaction> transactions(Long organisationId) {
        return within(organisationId, "terminal", "outlet", "merchant", ORGANISATION);
    }

    private static <T> Specification<T> within(Long organisationId, String... pathToOrganisation) {
        Objects.requireNonNull(organisationId, "organisationId");
        return (root, query, builder) -> {
            Path<?> organisation = root;
            for (String attribute : pathToOrganisation) {
                organisation = organisation.get(attribute);
            }
            return builder.equal(organisation.get(ORGANISATION_ID), organisationId);
        };
    }
}
