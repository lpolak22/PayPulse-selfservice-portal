package com.example.paypulse.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ActivateAccountRequest(
        @NotBlank String token,
        @NotBlank String password,
        @NotBlank String confirmPassword
) {
}
