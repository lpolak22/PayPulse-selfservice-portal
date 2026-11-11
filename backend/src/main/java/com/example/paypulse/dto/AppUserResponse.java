package com.example.paypulse.dto;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;

import java.time.LocalDateTime;

public record AppUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        boolean active,
        Long organisationId,
        String organisationName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
