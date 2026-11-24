package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.config.SevenPaySyncProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!sevenpay-sync & !sevenpay-import")
public class SevenPaySyncScheduler {

    private final SevenPaySyncService syncService;
    private final SevenPaySyncProperties properties;

    public SevenPaySyncScheduler(SevenPaySyncService syncService, SevenPaySyncProperties properties) {
        this.syncService = syncService;
        this.properties = properties;
    }

    @Scheduled(initialDelayString = "${sevenpay.sync.initial-delay:1m}", fixedDelayString = "${sevenpay.sync.interval:15m}")
    public void runScheduledSync() {
        if (properties.enabled()) {
            syncService.runOnce();
        }
    }
}
