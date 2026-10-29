package com.example.paypulse.service;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;

public interface AppUserService {

    AppUserResponse createUser(CreateAppUserRequest request);

    PageResponse<AppUserResponse> getUsers(AppUserSearchCriteria criteria, PageQuery pageQuery);

    AppUserResponse getUser(Long id);

    AppUserResponse updateUser(Long id, UpdateAppUserRequest request);

    AppUserResponse activateUser(Long id);

    AppUserResponse deactivateUser(Long id);

    void deleteUser(Long id);
}
