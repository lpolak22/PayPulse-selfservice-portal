package com.example.paypulse.seed.service;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.seed.config.SeedProperties;
import com.example.paypulse.seed.util.PasswordUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("seed")
public class UserSeedService {
    private final AppUserRepository appUserRepository;
    private final PasswordUtil passwordUtil;
    private final SeedProperties seedProperties;

    public UserSeedService(
            AppUserRepository appUserRepository,
            PasswordUtil passwordUtil,
            SeedProperties seedProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordUtil = passwordUtil;
        this.seedProperties = seedProperties;
    }

    @Transactional
    public void createUsersForOrganisation(Organisation organisation) {
        String orgSlug = organisation.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");

        createUser(organisation, "Admin", "One",
                "admin1." + orgSlug + "@paypulse.test", "+385911111111",
                UserRole.ADMIN, UserStatus.ACTIVE, true);
        createUser(organisation, "Admin", "Two",
                "admin2." + orgSlug + "@paypulse.test", "+385911111112",
                UserRole.ADMIN, UserStatus.ACTIVE, true);

        createUser(organisation, "User", "One",
                "user1." + orgSlug + "@paypulse.test", "+385922222221",
                UserRole.USER, UserStatus.ACTIVE, true);
        createUser(organisation, "User", "Two",
                "user2." + orgSlug + "@paypulse.test", "+385922222222",
                UserRole.USER, UserStatus.ACTIVE, true);
        createUser(organisation, "User", "Three",
                "user3." + orgSlug + "@paypulse.test", "+385922222223",
                UserRole.USER, UserStatus.ACTIVE, true);

        createUser(organisation, "Deactivated", "User",
                "deactivated." + orgSlug + "@paypulse.test", "+385933333333",
                UserRole.USER, UserStatus.INACTIVE, true);
        createUser(organisation, "Pending", "User",
                "pending." + orgSlug + "@paypulse.test", "+385944444444",
                UserRole.USER, UserStatus.PENDING_ACTIVATION, false);
    }

    private void createUser(
            Organisation organisation,
            String firstName,
            String lastName,
            String email,
            String phone,
            UserRole role,
            UserStatus status,
            boolean hasPassword
    ) {
        if (appUserRepository.existsUndeletedByEmail(email)) {
            return;
        }

        String hashedPassword = hasPassword
                ? passwordUtil.hash(seedProperties.getTestPassword())
                : null;

        AppUser user = AppUser.createPending(
                organisation, firstName, lastName, email, phone, role);
        user.changeStatus(status);
        user.initializeSeedSecurity(hashedPassword, !hasPassword);

        appUserRepository.save(user);
    }
}