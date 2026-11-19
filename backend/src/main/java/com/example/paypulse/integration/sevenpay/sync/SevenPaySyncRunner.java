package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.model.SyncRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sevenpay-sync")
public class SevenPaySyncRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SevenPaySyncRunner.class);

    private final SevenPaySyncService syncService;
    private final ApplicationContext context;

    public SevenPaySyncRunner(SevenPaySyncService syncService, ApplicationContext context) {
        this.syncService = syncService;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("7Pay sync starting");
        String status = syncService.runOnce().map(this::logRun).orElse(SevenPaySyncService.FAILED);
        int exitCode = SevenPaySyncService.FAILED.equals(status) ? 1 : 0;
        System.exit(SpringApplication.exit(context, () -> exitCode));
    }

    private String logRun(SyncRun run) {
        log.info("7Pay sync finished: {}, processed {}, skipped {}, failed {}{}", run.getStatus(),
                run.getRowsProcessed(), run.getRowsSkipped(), run.getRowsFailed(),
                run.getErrorMessage() == null ? "" : ", error: " + run.getErrorMessage());
        return run.getStatus();
    }
}
