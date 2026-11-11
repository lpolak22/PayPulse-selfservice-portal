package com.example.paypulse.service;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;

public interface LastAdminGuard {

    void ensureCanDelete(AppUser user);

    void ensureCanChangeStatus(AppUser user, UserStatus newStatus);

    void ensureCanChangeRole(AppUser user, UserRole newRole);
}
