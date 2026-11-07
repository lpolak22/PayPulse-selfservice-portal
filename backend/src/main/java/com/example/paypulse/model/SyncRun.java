package com.example.paypulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "sync_run")
public class SyncRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sync_run")
    private Long idSyncRun;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "rows_processed", nullable = false)
    private int rowsProcessed;

    @Column(name = "rows_skipped", nullable = false)
    private int rowsSkipped;

    @Column(name = "rows_failed", nullable = false)
    private int rowsFailed;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    protected SyncRun() {
    }

    public static SyncRun start() {
        SyncRun run = new SyncRun();
        run.startedAt = Instant.now();
        run.status = "RUNNING";
        return run;
    }

    public void finish(String status, int processed, int skipped, int failed, String errorMessage) {
        this.status = status;
        this.rowsProcessed = processed;
        this.rowsSkipped = skipped;
        this.rowsFailed = failed;
        this.errorMessage = errorMessage;
        this.finishedAt = Instant.now();
    }

    public Long getIdSyncRun() {
        return idSyncRun;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public int getRowsProcessed() {
        return rowsProcessed;
    }

    public int getRowsSkipped() {
        return rowsSkipped;
    }

    public int getRowsFailed() {
        return rowsFailed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
