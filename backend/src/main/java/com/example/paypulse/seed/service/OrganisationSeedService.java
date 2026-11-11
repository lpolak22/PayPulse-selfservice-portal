package com.example.paypulse.seed.service;

import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.OrganisationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("seed")
public class OrganisationSeedService {
    private final OrganisationRepository organisationRepository;

    public OrganisationSeedService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @Transactional
    public Organisation createOrganisation(String name) {
        return organisationRepository.findByName(name)
                .orElseGet(() -> {
                    Organisation organisation = new Organisation();
                    organisation.setName(name);
                    organisation.setStatus("ACTIVE");
                    return organisationRepository.save(organisation);
                });
    }
}