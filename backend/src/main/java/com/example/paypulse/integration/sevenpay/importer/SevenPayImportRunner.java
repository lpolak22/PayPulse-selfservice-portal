package com.example.paypulse.integration.sevenpay.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sevenpay-import")
public class SevenPayImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SevenPayImportRunner.class);

    private final SevenPayImportService importService;
    private final ApplicationContext context;

    public SevenPayImportRunner(SevenPayImportService importService, ApplicationContext context) {
        this.importService = importService;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("7Pay import starting");
        int exitCode = 0;
        try {
            importService.importAll().forEach(result -> log.info("7Pay import {}", result));
            log.info("7Pay import finished");
        } catch (RuntimeException exception) {
            log.error("7Pay import failed", exception);
            exitCode = 1;
        }
        int code = exitCode;
        System.exit(SpringApplication.exit(context, () -> code));
    }
}
