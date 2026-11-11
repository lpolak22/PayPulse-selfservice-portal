package com.example.paypulse.seed.service;

import com.example.paypulse.model.MccCode;
import com.example.paypulse.repository.MccCodeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("seed")
public class MccCodeSeedService {
    private final MccCodeRepository mccCodeRepository;

    public MccCodeSeedService(MccCodeRepository mccCodeRepository) {
        this.mccCodeRepository = mccCodeRepository;
    }

    @Transactional
    public MccCode createMccCode(String code, String description) {
        return mccCodeRepository.findById(code)
                .orElseGet(() -> mccCodeRepository.save(new MccCode(code, description)));
    }
}
