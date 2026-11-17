package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.outlet.OutletResponse;
import com.example.paypulse.dto.outlet.OutletSearchCriteria;
import com.example.paypulse.service.OutletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchants/{merchantId}/outlets")
@Tag(name = "Outlets", description = "Outlets of a merchant in the signed-in user's organisation")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "NOT_FOUND: the merchant does not exist in the user's organisation",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class OutletController {

    private final OutletService outletService;

    public OutletController(OutletService outletService) {
        this.outletService = outletService;
    }

    @GetMapping
    @Operation(summary = "List a merchant's outlets", description = "Sorted by outlet name.")
    @ApiResponse(responseCode = "200", description = "One page of outlets")
    public PageResponse<OutletResponse> getOutlets(
            @Parameter(description = "Internal merchant ID (id in the merchant list)")
            @PathVariable Long merchantId,
            @Parameter(description = "Case-insensitive partial match on the outlet name, or the exact 7Pay outlet ID")
            @RequestParam(required = false) String search,
            @Parameter(description = "Exact city name")
            @RequestParam(required = false) String city,
            @Parameter(description = "Case-insensitive status, e.g. ACTIVE")
            @RequestParam(required = false) String status,
            @ParameterObject PageQuery pageQuery) {
        return outletService.getOutlets(merchantId, new OutletSearchCriteria(search, city, status), pageQuery);
    }
}
