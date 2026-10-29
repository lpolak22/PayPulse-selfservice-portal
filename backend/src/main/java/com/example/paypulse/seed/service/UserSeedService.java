/*package com.example.paypulse.seed.service;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.seed.config.SeedProperties;
import com.example.paypulse.seed.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Service
public class UserSeedService {
    private static final Logger log = LoggerFactory.getLogger(UserSeedService.class);
    private final AppUserRepository appUserRepository;
    private final PasswordUtil passwordUtil;
    private final SeedProperties seedProperties;

    public UserSeedService(AppUserRepository appUserRepository, PasswordUtil passwordUtil, SeedProperties seedProperties) {
        this.appUserRepository = appUserRepository;
        this.passwordUtil = passwordUtil;
        this.seedProperties = seedProperties;
    }

    @Transactional
    public void createUsersForOrganisation(Organisation org) {
        String orgSlug = org.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");

        // 2 Admins
        createUser(org, "Admin", "One", "admin1." + orgSlug + "@paypulse.test", "+385911111111", UserRole.ADMIN, UserStatus.ACTIVE, true);
        createUser(org, "Admin", "Two", "admin2." + orgSlug + "@paypulse.test", "+385911111112", UserRole.ADMIN, UserStatus.ACTIVE, true);

        // 3 Users
        createUser(org, "User", "One", "user1." + orgSlug + "@paypulse.test", "+385922222221", UserRole.USER, UserStatus.ACTIVE, true);
        createUser(org, "User", "Two", "user2." + orgSlug + "@paypulse.test", "+385922222222", UserRole.USER, UserStatus.ACTIVE, true);
        createUser(org, "User", "Three", "user3." + orgSlug + "@paypulse.test", "+385922222223", UserRole.USER, UserStatus.ACTIVE, true);

        // 1 deactivated user
        createUser(org, "Deactivated", "User", "deactivated." + orgSlug + "@paypulse.test", "+385933333333", UserRole.USER, UserStatus.INACTIVE, true);

        // 1 pending user (is_first_login = true)
        createUser(org, "Pending", "User", "pending." + orgSlug + "@paypulse.test", "+385944444444", UserRole.USER, UserStatus.PENDING_ACTIVATION, false);
    }

    private void createUser(Organisation org, String firstName, String lastName, String email, String phone,
                            UserRole role, UserStatus status, boolean hasPassword) {
        if (appUserRepository.existsUndeletedByEmail(email)) {
            return;
        }
        AppUser user = AppUser.createPending(org, firstName, lastName, email, phone, role);
        user.changeStatus(status);
        if (hasPassword) {
            try {
                Field passwordField = AppUser.class.getDeclaredField("passwordHash");
                passwordField.setAccessible(true);
                passwordField.set(user, passwordUtil.hash(seedProperties.getTestPassword()));
            } catch (Exception e) {
                log.error("Error setting password", e);
            }
        } else {
            try {
                Field passwordField = AppUser.class.getDeclaredField("passwordHash");
                passwordField.setAccessible(true);
                passwordField.set(user, null);
            } catch (Exception e) {
                log.error("Error clearing password", e);
            }
        }
        try {
            Field deletedField = AppUser.class.getDeclaredField("deletedAt");
            deletedField.setAccessible(true);
            deletedField.set(user, null);
            Field activeField = AppUser.class.getDeclaredField("active");
            activeField.setAccessible(true);
            activeField.set(user, status == UserStatus.ACTIVE);
        } catch (Exception e) {
            log.error("Error setting active/deleted state", e);
        }
        try {
            Field firstLoginField = AppUser.class.getDeclaredField("isFirstLogin");
            firstLoginField.setAccessible(true);
            firstLoginField.set(user, !hasPassword);
        } catch (Exception e) {
            // ignore
        }
        try {
            Field totpEnabledField = AppUser.class.getDeclaredField("totpEnabled");
            totpEnabledField.setAccessible(true);
            totpEnabledField.set(user, Boolean.FALSE);
        } catch (Exception e) {
            // ignore
        }
        try {
            Field failedLoginField = AppUser.class.getDeclaredField("failedLoginCount");
            failedLoginField.setAccessible(true);
            failedLoginField.set(user, (short) 0);
        } catch (Exception e) {
            // ignore
        }
        appUserRepository.save(user);
    }
}
*/

package com.example.paypulse.seed.service;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.seed.config.SeedProperties;
import com.example.paypulse.seed.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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