package com.example.paypulse.mapper;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUser toNewUser(CreateAppUserRequest request, Organisation organisation) {
        return AppUser.createPending(
                organisation,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.role()
        );
    }

    public AppUserResponse toResponse(AppUser user) {
        Organisation organisation = user.getOrganisation();

        return new AppUserResponse(
                user.getIdUser(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.isActive(),
                organisation == null ? null : organisation.getIdOrganisation(),
                organisation == null ? null : organisation.getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
