package com.example.paypulse.integration.sevenpay.sync;

import java.util.EnumMap;
import java.util.Map;

public class SyncCounts {

    private final Map<WriteOutcome, Integer> written = new EnumMap<>(WriteOutcome.class);
    private final Map<SyncSkipReason, Integer> skipped = new EnumMap<>(SyncSkipReason.class);
    private int failed;

    public void add(WriteOutcome outcome) {
        written.merge(outcome, 1, Integer::sum);
    }

    public void skip(SyncSkipReason reason) {
        skipped.merge(reason, 1, Integer::sum);
    }

    public void fail() {
        failed++;
    }

    public int processed() {
        return written.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int skippedTotal() {
        return skipped.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int failed() {
        return failed;
    }

    public int written(WriteOutcome outcome) {
        return written.getOrDefault(outcome, 0);
    }

    public int skipped(SyncSkipReason reason) {
        return skipped.getOrDefault(reason, 0);
    }

    @Override
    public String toString() {
        return "written " + written + ", skipped " + skipped + ", failed " + failed;
    }
}
