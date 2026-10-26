package com.example.paypulse.dto;

import java.time.LocalDateTime;

public record AppUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String role,
        String status,
        boolean active,
        Long organisationId,
        String organisationName,
        LocalDateTime createdAt
) {
}
