package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Operation(summary = "List merchants", description = "Only merchants of the user's organisation. Sorted by name.")
    @ApiResponse(responseCode = "200", description = "One page of merchants")
    public PageResponse<MerchantResponse> getMerchants(@ParameterObject PageQuery pageQuery) {
        return merchantService.getMerchants(pageQuery);
    }
}
