package com.example.paypulse.seed.service;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.repository.AcquirerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcquirerSeedService {
    private final AcquirerRepository acquirerRepository;

    public AcquirerSeedService(AcquirerRepository acquirerRepository) {
        this.acquirerRepository = acquirerRepository;
    }

    @Transactional
    public Acquirer createAcquirer(String code, String name) {
        return acquirerRepository.findByCode(code)
                .orElseGet(() -> acquirerRepository.save(new Acquirer(code, name)));
    }
}
