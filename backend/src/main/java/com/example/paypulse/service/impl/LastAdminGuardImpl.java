package com.example.paypulse.service.impl;

import com.example.paypulse.exception.LastAdminException;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.LastAdminGuard;
import org.springframework.stereotype.Component;

@Component
public class LastAdminGuardImpl implements LastAdminGuard {

    private final AppUserRepository appUserRepository;

    public LastAdminGuardImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void ensureCanDelete(AppUser user) {
        ensureAnotherActiveAdminRemains(user);
    }

    @Override
    public void ensureCanChangeStatus(AppUser user, UserStatus newStatus) {
        if (newStatus != UserStatus.ACTIVE) {
            ensureAnotherActiveAdminRemains(user);
        }
    }

    @Override
    public void ensureCanChangeRole(AppUser user, UserRole newRole) {
        if (newRole != UserRole.ADMIN) {
            ensureAnotherActiveAdminRemains(user);
        }
    }

    private void ensureAnotherActiveAdminRemains(AppUser user) {
        if (!isActiveAdmin(user)) {
            return;
        }
        boolean anotherAdminRemains = appUserRepository.existsOtherActiveUserWithRole(
                user.getOrganisation().getIdOrganisation(), UserRole.ADMIN, user.getIdUser());
        if (!anotherAdminRemains) {
            throw new LastAdminException();
        }
    }

    private static boolean isActiveAdmin(AppUser user) {
        return user.getRole() == UserRole.ADMIN && user.isActive();
    }
}
