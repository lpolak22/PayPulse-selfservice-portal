package com.example.paypulse.service.impl;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.exception.UserEmailAlreadyExistsException;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.repository.AppUserSpecifications;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AppUserService;
import com.example.paypulse.service.AuditLogService;
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
    private final Clock clock;

    public AppUserServiceImpl(AppUserRepository appUserRepository,
                              OrganisationRepository organisationRepository,
                              AppUserMapper appUserMapper,
                              AuditLogService auditLogService,
                              CurrentUserProvider currentUserProvider,
                              Clock clock) {
        this.appUserRepository = appUserRepository;
        this.organisationRepository = organisationRepository;
        this.appUserMapper = appUserMapper;
        this.auditLogService = auditLogService;
        this.currentUserProvider = currentUserProvider;
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
    public void deleteUser(Long id) {
        AppUser user = findUser(id);
        AppUserResponse before = appUserMapper.toResponse(user);

        user.markDeleted(LocalDateTime.now(clock));

        auditLogService.record(AuditAction.USER_DELETED, id, before, null);
    }

    private void applyChanges(AppUser user, UpdateAppUserRequest request) {
        applyIfPresent(request.email(), email -> changeEmail(user, email));
        applyIfPresent(request.firstName(), user::changeFirstName);
        applyIfPresent(request.lastName(), user::changeLastName);
        applyIfPresent(request.phone(), user::changePhone);
        applyIfPresent(request.role(), user::changeRole);
        applyIfPresent(request.status(), status -> changeStatus(user, status));
    }

    private void changeEmail(AppUser user, String email) {
        if (appUserRepository.existsUndeletedByEmailExcludingUser(email, user.getIdUser())) {
            throw new UserEmailAlreadyExistsException(email);
        }
        user.changeEmail(email);
    }

    // Admins can activate or deactivate a user, but only the activation flow leaves PENDING_ACTIVATION.
    private void changeStatus(AppUser user, UserStatus status) {
        if (status == UserStatus.PENDING_ACTIVATION && user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Status cannot be changed back to PENDING_ACTIVATION.");
        }
        user.changeStatus(status);
    }

    private AppUser findUser(Long id) {
        return appUserRepository.findUndeletedInOrganisation(id, currentOrganisationId())
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Long currentOrganisationId() {
        return currentUserProvider.getCurrentUser().organisationId();
    }

    // The unique index is the final guard when two requests claim the same email at the same time.
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
