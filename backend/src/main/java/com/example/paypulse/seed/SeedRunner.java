package com.example.paypulse.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed & !prod & !production")
public class SeedRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final SeedOrchestrator seedOrchestrator;

    public SeedRunner(SeedOrchestrator seedOrchestrator) {
        this.seedOrchestrator = seedOrchestrator;
    }

    @Override
    public void run(String... args) {
        log.info("Seed runner starting");
        seedOrchestrator.orchestrate();
        log.info("Seed runner finished");
    }
}