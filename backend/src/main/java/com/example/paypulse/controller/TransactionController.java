package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.dto.transaction.TransactionSearchCriteria;
import com.example.paypulse.dto.transaction.TransactionSort;
import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.SalesChannel;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import com.example.paypulse.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Search the transactions of the signed-in user's organisation")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class TransactionController {

    private static final String EXACT_MATCH = "Exact match";

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Search transactions",
            description = "from is inclusive and to is exclusive, both ISO-8601 with an offset. Without to, now is "
                    + "used; without from, 7 days before to. The range is at most 13 months. Values inside one "
                    + "list filter are combined with OR, different filters with AND. Times in the response are UTC.")
    @ApiResponse(responseCode = "200", description = "One page of transactions")
    @ApiResponse(responseCode = "400", description = "RANGE_TOO_LARGE: the range is longer than 13 months; "
            + "BAD_REQUEST: from is after to, a date has no offset, or a filter, sort or direction value is unknown",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<TransactionResponse> searchTransactions(
            @Parameter(description = "Start, inclusive", example = "2026-11-01T00:00:00+01:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "End, exclusive", example = "2026-11-08T00:00:00+01:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) List<TransactionStatus> status,
            @RequestParam(required = false) List<TransactionType> type,
            @RequestParam(required = false) List<CardBrand> cardBrand,
            @RequestParam(required = false) List<SalesChannel> salesChannel,
            @Parameter(description = EXACT_MATCH) @RequestParam(required = false) String authCode,
            @Parameter(description = EXACT_MATCH) @RequestParam(required = false) String rrn,
            @RequestParam(required = false) Long outletId,
            @Parameter(description = "Terminal ID, exact match") @RequestParam(required = false) String terminalId,
            @Parameter(schema = @Schema(allowableValues = {"date", "amount"}))
            @RequestParam(defaultValue = "date") String sort,
            @Parameter(schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String direction,
            @ParameterObject PageQuery pageQuery) {
        TransactionSearchCriteria criteria = new TransactionSearchCriteria(from, to, status, type, cardBrand,
                salesChannel, authCode, rrn, outletId, terminalId);
        return transactionService.search(criteria, TransactionSort.of(sort, direction), pageQuery);
    }
}
