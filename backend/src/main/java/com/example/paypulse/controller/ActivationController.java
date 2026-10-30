package com.example.paypulse.controller;

import com.example.paypulse.dto.auth.ActivateAccountRequest;
import com.example.paypulse.dto.auth.ActivationStatusResponse;
import com.example.paypulse.service.ActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/activation")
@Tag(name = "Activation", description = "Set the first password from the activation link")
public class ActivationController {

    private final ActivationService activationService;

    public ActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Check an activation link", description = "Returns VALID, EXPIRED or USED.")
    public ActivationStatusResponse checkToken(@PathVariable String token) {
        return new ActivationStatusResponse(activationService.checkToken(token));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set the first password and activate the account")
    public void activate(@Valid @RequestBody ActivateAccountRequest request) {
        activationService.activate(request);
    }
}
