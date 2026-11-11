package com.example.paypulse.service.impl;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ConflictException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.exception.UserEmailAlreadyExistsException;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.repository.AppUserSpecifications;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.ActivationService;
import com.example.paypulse.service.AppUserService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.LastAdminGuard;
import com.example.paypulse.service.RefreshTokenService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private static final Sort DEFAULT_SORT = Sort.by("lastName", "firstName", "idUser");

    private final AppUserRepository appUserRepository;
    private final OrganisationRepository organisationRepository;
    private final AppUserMapper appUserMapper;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;
    private final LastAdminGuard lastAdminGuard;
    private final RefreshTokenService refreshTokenService;
    private final ActivationService activationService;
    private final Clock clock;

    public AppUserServiceImpl(AppUserRepository appUserRepository,
                              OrganisationRepository organisationRepository,
                              AppUserMapper appUserMapper,
                              AuditLogService auditLogService,
                              CurrentUserProvider currentUserProvider,
                              LastAdminGuard lastAdminGuard,
                              RefreshTokenService refreshTokenService,
                              ActivationService activationService,
                              Clock clock) {
        this.appUserRepository = appUserRepository;
        this.organisationRepository = organisationRepository;
        this.appUserMapper = appUserMapper;
        this.auditLogService = auditLogService;
        this.currentUserProvider = currentUserProvider;
        this.lastAdminGuard = lastAdminGuard;
        this.refreshTokenService = refreshTokenService;
        this.activationService = activationService;
        this.clock = clock;
    }

    @Override
    public AppUserResponse createUser(CreateAppUserRequest request) {
        if (appUserRepository.existsUndeletedByEmail(request.email())) {
            throw new UserEmailAlreadyExistsException(request.email());
        }
        Organisation organisation = organisationRepository.getReferenceById(currentOrganisationId());
        AppUser user = saveAndFlush(appUserMapper.toNewUser(request, organisation));

        AppUserResponse created = appUserMapper.toResponse(user);
        auditLogService.record(AuditAction.USER_CREATED, user.getIdUser(), null, created);
        activationService.startActivation(user);
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppUserResponse> getUsers(AppUserSearchCriteria criteria, PageQuery pageQuery) {
        return PageResponse.from(
                appUserRepository.findAll(AppUserSpecifications.matching(currentOrganisationId(), criteria),
                        pageQuery.toPageable(DEFAULT_SORT)),
                appUserMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserResponse getUser(Long id) {
        return appUserMapper.toResponse(findUser(id));
    }

    @Override
    public AppUserResponse updateUser(Long id, UpdateAppUserRequest request) {
        AppUser user = findUser(id);
        AppUserResponse before = appUserMapper.toResponse(user);

        applyChanges(user, request);
        AppUserResponse after = appUserMapper.toResponse(saveAndFlush(user));

        auditLogService.record(AuditAction.USER_UPDATED, id, before, after);
        return after;
    }

    @Override
    public AppUserResponse activateUser(Long id) {
        return changeStatus(id, UserStatus.ACTIVE, AuditAction.USER_ACTIVATED);
    }

    @Override
    public AppUserResponse deactivateUser(Long id) {
        return changeStatus(id, UserStatus.INACTIVE, AuditAction.USER_DEACTIVATED);
    }

    @Override
    public void deleteUser(Long id) {
        AppUser user = findUser(id);
        lastAdminGuard.ensureCanDelete(user);
        AppUserResponse before = appUserMapper.toResponse(user);

        user.markDeleted(LocalDateTime.now(clock));
        refreshTokenService.revokeAllForUser(id);

        auditLogService.record(AuditAction.USER_DELETED, id, before, null);
    }

    @Override
    public void resendActivation(Long id) {
        AppUser user = findUser(id);
        if (!Boolean.TRUE.equals(user.getIsFirstLogin()) || user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            throw new ConflictException("Only users who have not completed activation can get a new activation email.");
        }
        user.markActivationResent(LocalDateTime.now(clock));
        activationService.startActivation(saveAndFlush(user));
    }

    private AppUserResponse changeStatus(Long id, UserStatus status, AuditAction action) {
        AppUser user = findUser(id);
        AppUserResponse before = appUserMapper.toResponse(user);
        if (user.getStatus() == status) {
            return before;
        }
        changeStatus(user, status);
        AppUserResponse after = appUserMapper.toResponse(saveAndFlush(user));

        auditLogService.record(action, id, before, after);
        return after;
    }

    private void applyChanges(AppUser user, UpdateAppUserRequest request) {
        applyIfPresent(request.email(), email -> changeEmail(user, email));
        applyIfPresent(request.firstName(), user::changeFirstName);
        applyIfPresent(request.lastName(), user::changeLastName);
        applyIfPresent(request.phone(), user::changePhone);
        applyIfPresent(request.role(), role -> changeRole(user, role));
        applyIfPresent(request.status(), status -> changeStatus(user, status));
    }

    private void changeEmail(AppUser user, String email) {
        if (appUserRepository.existsUndeletedByEmailExcludingUser(email, user.getIdUser())) {
            throw new UserEmailAlreadyExistsException(email);
        }
        user.changeEmail(email);
    }

    private void changeRole(AppUser user, UserRole role) {
        lastAdminGuard.ensureCanChangeRole(user, role);
        user.changeRole(role);
    }

    private void changeStatus(AppUser user, UserStatus status) {
        ensureStatusTransitionAllowed(user, status);
        lastAdminGuard.ensureCanChangeStatus(user, status);

        boolean wasActive = user.isActive();
        user.changeStatus(status);
        if (wasActive && !user.isActive()) {
            refreshTokenService.revokeAllForUser(user.getIdUser());
        }
    }

    private static void ensureStatusTransitionAllowed(AppUser user, UserStatus status) {
        if (status == user.getStatus()) {
            return;
        }
        if (status == UserStatus.PENDING_ACTIVATION) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Status cannot be changed back to PENDING_ACTIVATION.");
        }
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION && status == UserStatus.ACTIVE) {
            throw new ConflictException("The user has not completed activation yet: "
                    + "the account is activated through the activation email.");
        }
    }

    private AppUser findUser(Long id) {
        return appUserRepository.findUndeletedInOrganisation(id, currentOrganisationId())
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Long currentOrganisationId() {
        return currentUserProvider.getCurrentUser().organisationId();
    }

    private AppUser saveAndFlush(AppUser user) {
        try {
            return appUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueViolation(exception)) {
                throw new UserEmailAlreadyExistsException(user.getEmail());
            }
            throw exception;
        }
    }

    private static boolean isEmailUniqueViolation(DataIntegrityViolationException exception) {
        return exception.getCause() instanceof ConstraintViolationException violation
                && AppUser.EMAIL_UNIQUE_INDEX.equals(violation.getConstraintName());
    }

    private static <T> void applyIfPresent(T value, Consumer<T> change) {
        if (value != null) {
            change.accept(value);
        }
    }
}
