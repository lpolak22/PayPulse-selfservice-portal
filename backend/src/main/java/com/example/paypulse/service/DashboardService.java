package com.example.paypulse.service;

import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;

import java.time.LocalDate;

public interface DashboardService {

    DashboardSummaryResponse getSummary(LocalDate from, LocalDate to);
}
