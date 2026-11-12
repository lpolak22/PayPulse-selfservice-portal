package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.dto.merchant.MerchantSearchCriteria;
import com.example.paypulse.dto.merchant.MerchantSort;
import com.example.paypulse.service.MerchantService;
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
@RequestMapping("/api/merchants")
@Tag(name = "Merchants", description = "Merchants of the signed-in user's organisation")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    @Operation(summary = "List merchants",
            description = "Only merchants of the user's organisation. Sorted by name unless sort is given.")
    @ApiResponse(responseCode = "200", description = "One page of merchants")
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR: oib is not 11 digits or status is unknown; "
            + "BAD_REQUEST: the sort or direction value is unknown",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<MerchantResponse> getMerchants(
            @Parameter(description = "Case-insensitive match on name, or exact match on ID or OIB")
            @RequestParam(required = false) String search,
            @Parameter(description = "Exact OIB, 11 digits")
            @RequestParam(required = false) String oib,
            @Parameter(description = "Case-insensitive status",
                    schema = @Schema(allowableValues = {"ACTIVE", "DEACTIVATED", "HOLD"}))
            @RequestParam(required = false) String status,
            @Parameter(schema = @Schema(allowableValues = {"name", "oib", "status"}))
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String direction,
            @ParameterObject PageQuery pageQuery) {
        return merchantService.getMerchants(new MerchantSearchCriteria(search, oib, status),
                MerchantSort.of(sort, direction), pageQuery);
    }
}
