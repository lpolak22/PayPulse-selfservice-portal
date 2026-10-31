package com.example.paypulse.security;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import com.example.paypulse.repository.OrganisationScope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserOrganisationScope implements CurrentOrganisationScope {

    private final CurrentUserProvider currentUserProvider;

    public CurrentUserOrganisationScope(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Long organisationId() {
        return currentUserProvider.getCurrentUser().organisationId();
    }

    @Override
    public Specification<AppUser> users() {
        return OrganisationScope.users(organisationId());
    }

    @Override
    public Specification<Merchant> merchants() {
        return OrganisationScope.merchants(organisationId());
    }

    @Override
    public Specification<Outlet> outlets() {
        return OrganisationScope.outlets(organisationId());
    }

    @Override
    public Specification<Terminal> terminals() {
        return OrganisationScope.terminals(organisationId());
    }

    @Override
    public Specification<Transaction> transactions() {
        return OrganisationScope.transactions(organisationId());
    }
}
