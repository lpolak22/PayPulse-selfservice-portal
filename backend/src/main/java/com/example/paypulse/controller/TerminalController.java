package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.terminal.TerminalResponse;
import com.example.paypulse.dto.terminal.TerminalSearchCriteria;
import com.example.paypulse.dto.terminal.TerminalSort;
import com.example.paypulse.service.TerminalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/terminals")
@Tag(name = "Terminals", description = "Terminals of the signed-in user's organisation")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @GetMapping
    @Operation(summary = "List terminals",
            description = "Only terminals of the user's organisation, across all its merchants and outlets. "
                    + "Sorted by TID unless sort is given. type and status are returned as stored.")
    @ApiResponse(responseCode = "200", description = "One page of terminals")
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR: status or type is unknown; "
            + "BAD_REQUEST: the sort or direction value is unknown",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<TerminalResponse> getTerminals(
            @Parameter(description = "Case-insensitive status", schema = @Schema(allowableValues = {
                    "PREPARED", "AUTHENTICATED", "ACTIVE", "INACTIVE", "DEACTIVATED", "SUSPENDED", "BLOCKED"}))
            @RequestParam(required = false) String status,
            @Parameter(description = "Terminal type, ignoring case, spaces and underscores", schema = @Schema(
                    allowableValues = {"SoftPOS V1", "SoftPOS V2", "POS", "MOBILE", "ECR", "VIRTUAL"}))
            @RequestParam(required = false) String type,
            @Parameter(schema = @Schema(allowableValues = {"tid", "type", "status", "outlet", "merchant"}))
            @RequestParam(defaultValue = "tid") String sort,
            @Parameter(schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String direction,
            @ParameterObject PageQuery pageQuery) {
        return terminalService.getTerminals(new TerminalSearchCriteria(status, type),
                TerminalSort.of(sort, direction), pageQuery);
    }
}
