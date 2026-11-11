package com.example.paypulse.dto;

import com.example.paypulse.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Size(max = 254) @Email(regexp = ValidationPatterns.EMAIL) String email,
        @Pattern(regexp = ValidationPatterns.E164_PHONE, message = ValidationPatterns.E164_PHONE_MESSAGE) String phone,
        @NotNull UserRole role
) {
}
