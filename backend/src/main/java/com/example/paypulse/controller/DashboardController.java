package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;
import com.example.paypulse.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Transaction totals of the signed-in user's organisation, ready for charts")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get the dashboard summary",
            description = "Dates are inclusive days in Europe/Zagreb. Without from and to, the last 30 days "
                    + "including today are used. A single day gives an hourly trend (24 points), a longer range "
                    + "a daily trend with one point per day. previousTotalAmount covers the same number of days "
                    + "right before from. latestTransactions are the 5 newest transactions of any status.")
    @ApiResponse(responseCode = "200", description = "The summary")
    @ApiResponse(responseCode = "400", description = "BAD_REQUEST: a date is not in yyyy-MM-dd format",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public DashboardSummaryResponse getSummary(
            @Parameter(description = "First day, inclusive (yyyy-MM-dd)", example = "2026-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Last day, inclusive (yyyy-MM-dd)", example = "2026-10-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dashboardService.getSummary(from, to);
    }
}
