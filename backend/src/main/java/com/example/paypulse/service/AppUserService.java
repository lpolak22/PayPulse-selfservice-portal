package com.example.paypulse.service;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;

/**
 * Manages the users of the current user's organisation.
 */
public interface AppUserService {

    AppUserResponse createUser(CreateAppUserRequest request);

    PageResponse<AppUserResponse> getUsers(AppUserSearchCriteria criteria, PageQuery pageQuery);

    AppUserResponse getUser(Long id);

    AppUserResponse updateUser(Long id, UpdateAppUserRequest request);

    void deleteUser(Long id);
}
