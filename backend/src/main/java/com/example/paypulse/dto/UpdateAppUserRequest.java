package com.example.paypulse.dto;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update: a null field means "leave unchanged".
 */
public record UpdateAppUserRequest(
        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        @Size(max = 50) String firstName,

        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        @Size(max = 50) String lastName,

        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        @Size(max = 254) @Email(regexp = ValidationPatterns.EMAIL) String email,

        @Pattern(regexp = ValidationPatterns.E164_PHONE, message = ValidationPatterns.E164_PHONE_MESSAGE) String phone,

        UserRole role,

        UserStatus status
) {
}
