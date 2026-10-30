package com.example.paypulse.security;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

public interface CurrentOrganisationScope {

    Long organisationId();

    Specification<AppUser> users();

    Specification<Merchant> merchants();

    Specification<Outlet> outlets();

    Specification<Terminal> terminals();

    Specification<Transaction> transactions();
}
