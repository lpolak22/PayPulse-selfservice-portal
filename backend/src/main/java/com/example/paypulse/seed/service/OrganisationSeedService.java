/* package com.example.paypulse.seed.service;

import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganisationSeedService {
    private final OrganisationRepository organisationRepository;

    public OrganisationSeedService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @Transactional
    public Organisation createOrganisation(String name) {
        return organisationRepository.findAll().stream()
                .filter(o -> name.equals(o.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Organisation org = new Organisation();
                    org.setName(name);
                    org.setStatus("ACTIVE");
                    try {
                        java.lang.reflect.Field created = Organisation.class.getDeclaredField("createdAt");
                        created.setAccessible(true);
                        created.set(org, java.time.LocalDateTime.now());
                        java.lang.reflect.Field updated = Organisation.class.getDeclaredField("updatedAt");
                        updated.setAccessible(true);
                        updated.set(org, java.time.LocalDateTime.now());
                    } catch (Exception ignored) {
                    }
                    return organisationRepository.save(org);
                });
    }
}
*/

package com.example.paypulse.seed.service;

import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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