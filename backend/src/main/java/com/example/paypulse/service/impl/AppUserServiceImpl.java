package com.example.paypulse.service.impl;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.AppUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;

    public AppUserServiceImpl(AppUserRepository appUserRepository, AppUserMapper appUserMapper) {
        this.appUserRepository = appUserRepository;
        this.appUserMapper = appUserMapper;
    }

    @Override
    public List<AppUserResponse> getUsers() {
        return appUserRepository.findAllByDeletedAtIsNullOrderByLastNameAscFirstNameAsc()
                .stream()
                .map(appUserMapper::toResponse)
                .toList();
    }
}
