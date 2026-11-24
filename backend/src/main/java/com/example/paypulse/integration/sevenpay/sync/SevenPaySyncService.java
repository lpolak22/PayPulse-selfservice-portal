package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.config.SevenPayImportProperties;
import com.example.paypulse.config.SevenPaySyncProperties;
import com.example.paypulse.integration.sevenpay.SevenPayException;
import com.example.paypulse.integration.sevenpay.importer.SevenPayImportService;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.SyncRun;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.repository.SyncRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SevenPaySyncService {

    static final String SUCCESS = "SUCCESS";
    static final String PARTIAL = "PARTIAL";
    static final String FAILED = "FAILED";

    private static final Logger log = LoggerFactory.getLogger(SevenPaySyncService.class);
    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final int MAX_ERROR_LENGTH = 1000;

    private final SevenPayImportService importService;
    private final SevenPayTransactionSync transactionSync;
    private final OrganisationRepository organisationRepository;
    private final SyncRunRepository syncRunRepository;
    private final SevenPayImportProperties importProperties;
    private final SevenPaySyncProperties syncProperties;
    private final AtomicBoolean running = new AtomicBoolean();

    public SevenPaySyncService(SevenPayImportService importService, SevenPayTransactionSync transactionSync,
                               OrganisationRepository organisationRepository, SyncRunRepository syncRunRepository,
                               SevenPayImportProperties importProperties, SevenPaySyncProperties syncProperties) {
        this.importService = importService;
        this.transactionSync = transactionSync;
        this.organisationRepository = organisationRepository;
        this.syncRunRepository = syncRunRepository;
        this.importProperties = importProperties;
        this.syncProperties = syncProperties;
    }

    public Optional<SyncRun> runOnce() {
        if (!running.compareAndSet(false, true)) {
            log.info("7Pay sync is already running, this run is skipped");
            return Optional.empty();
        }
        try {
            return Optional.of(run());
        } finally {
            running.set(false);
        }
    }

    private SyncRun run() {
        LocalDate until = LocalDate.now(ZAGREB);
        LocalDate from = windowStart(until);
        SyncRun syncRun = syncRunRepository.save(SyncRun.start());
        SyncCounts counts = new SyncCounts();
        try {
            importMerchantsAndTerminals(counts);
            transactionSync.sync(organisation(), from, until, counts);
            syncRun.finish(statusOf(counts), counts.processed(), counts.skippedTotal(), counts.failed(), null);
        } catch (RuntimeException exception) {
            log.error("7Pay sync failed", exception);
            syncRun.finish(FAILED, counts.processed(), counts.skippedTotal(), counts.failed(), message(exception));
        }
        return syncRunRepository.save(syncRun);
    }

    private void importMerchantsAndTerminals(SyncCounts counts) {
        try {
            importService.importAll().forEach(result -> log.info("7Pay import {}", result));
        } catch (SevenPayException exception) {
            if (exception.isAccessDenied() || exception.isRetryable()) {
                throw exception;
            }
            log.warn("7Pay import failed, transactions are synced with the merchants already in the database: {}",
                    exception.getMessage());
            counts.fail();
        }
    }

    private static String statusOf(SyncCounts counts) {
        if (counts.failed() == 0) {
            return SUCCESS;
        }
        return counts.processed() + counts.skippedTotal() > 0 ? PARTIAL : FAILED;
    }

    private LocalDate windowStart(LocalDate until) {
        return syncRunRepository.findFirstByStatusInOrderByStartedAtDesc(List.of(SUCCESS, PARTIAL))
                .map(last -> LocalDate.ofInstant(last.getStartedAt(), ZAGREB).minusDays(syncProperties.overlapDays()))
                .orElse(until.minusDays(syncProperties.initialDays()));
    }

    private Organisation organisation() {
        return organisationRepository.findByName(importProperties.organisation())
                .orElseThrow(() -> new IllegalStateException(
                        "Organisation '" + importProperties.organisation() + "' not found, run the seed first"));
    }

    private static String message(RuntimeException exception) {
        String message = exception.getClass().getSimpleName() + ": " + exception.getMessage();
        return message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message;
    }
}
