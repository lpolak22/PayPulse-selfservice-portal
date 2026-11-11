package com.example.paypulse.service;

import java.time.Instant;

public record TransactionRange(Instant start, Instant end) {
}
