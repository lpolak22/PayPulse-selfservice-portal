package com.example.paypulse.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank @Pattern(regexp = "[^\\x00]*", message = "must not contain a NULL character") String email,
        @NotBlank String password
) {
}
