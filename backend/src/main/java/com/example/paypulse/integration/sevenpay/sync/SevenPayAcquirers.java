package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.repository.AcquirerRepository;
import org.springframework.stereotype.Component;

@Component
public class SevenPayAcquirers {

    static final String CODE_PREFIX = "7PAY-";
    static final String UNKNOWN_HOST = "UNKNOWN";

    private final AcquirerRepository acquirerRepository;

    public SevenPayAcquirers(AcquirerRepository acquirerRepository) {
        this.acquirerRepository = acquirerRepository;
    }

    public Acquirer resolve(String acquirerHost) {
        String host = acquirerHost == null || acquirerHost.isBlank() ? UNKNOWN_HOST : acquirerHost.strip();
        String code = CODE_PREFIX + host;
        return acquirerRepository.findByCode(code)
                .orElseGet(() -> acquirerRepository.save(new Acquirer(code, "7Pay acquirer " + host)));
    }
}
