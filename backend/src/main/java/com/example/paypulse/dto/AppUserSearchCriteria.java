package com.example.paypulse.dto;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;

public record AppUserSearchCriteria(String search, UserRole role, UserStatus status) {

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }
}
