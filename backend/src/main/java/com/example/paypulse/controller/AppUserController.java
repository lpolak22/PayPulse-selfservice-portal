package com.example.paypulse.controller;

import com.example.paypulse.config.SecurityConfig;
import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Manage the user accounts of the signed-in admin's organisation")
@SecurityRequirement(name = SecurityConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: not signed in",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "FORBIDDEN: the signed-in user is not an admin",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    @Operation(summary = "Create a user",
            description = "The new user starts in status PENDING_ACTIVATION and receives an activation email.")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR (see fieldErrors) or BAD_REQUEST",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "USER_EMAIL_EXISTS: the email is already used",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AppUserResponse> createUser(@Valid @RequestBody CreateAppUserRequest request) {
        AppUserResponse created = appUserService.createUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List users", description = "Deleted users are not listed. Sorted by last and first name.")
    @ApiResponse(responseCode = "200", description = "One page of users")
    @ApiResponse(responseCode = "400", description = "BAD_REQUEST: unknown role or status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<AppUserResponse> getUsers(
            @Parameter(description = "Case-insensitive match on full name or email")
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @ParameterObject PageQuery pageQuery) {
        return appUserService.getUsers(new AppUserSearchCriteria(search, role, status), pageQuery);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user")
    @ApiResponse(responseCode = "200", description = "The user")
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public AppUserResponse getUser(@PathVariable Long id) {
        return appUserService.getUser(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a user", description = "Only the fields present in the body are changed. "
            + "Setting the status to INACTIVE ends the user's sessions like the deactivate route.")
    @ApiResponse(responseCode = "200", description = "The updated user")
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR (see fieldErrors) or BAD_REQUEST",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "USER_EMAIL_EXISTS: the email is already used; "
            + "LAST_ADMIN: the last active admin cannot be deactivated or demoted; "
            + "CONFLICT: a user who has not completed activation cannot be set ACTIVE",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public AppUserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateAppUserRequest request) {
        return appUserService.updateUser(id, request);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a user", description = "Sets the status to ACTIVE (and is_active). "
            + "A reactivated user logs in with their existing password. Activating an active user changes nothing.")
    @ApiResponse(responseCode = "200", description = "The activated user")
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "CONFLICT: the user has not completed activation yet",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public AppUserResponse activateUser(@PathVariable Long id) {
        return appUserService.activateUser(id);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user", description = "Sets the status to INACTIVE (and is_active), revokes "
            + "all refresh tokens of the user and rejects their access token on the next request.")
    @ApiResponse(responseCode = "200", description = "The deactivated user")
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "LAST_ADMIN: the user is the last active admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public AppUserResponse deactivateUser(@PathVariable Long id) {
        return appUserService.deactivateUser(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user", description = "Soft delete: the user is kept but no longer listed. "
            + "All refresh tokens of the user are revoked.")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "LAST_ADMIN: the user is the last active admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
    }

    @PostMapping("/{id}/resend-activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Resend the activation email",
            description = "Sends a new activation link. Older links of the user stop working.")
    @ApiResponse(responseCode = "204", description = "Activation email sent")
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "CONFLICT: the user has already completed activation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void resendActivation(@PathVariable Long id) {
        appUserService.resendActivation(id);
    }
}
