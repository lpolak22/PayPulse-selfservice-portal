package com.example.paypulse.service;

import com.example.paypulse.dto.AppUserResponse;

import java.util.List;

public interface AppUserService {

    List<AppUserResponse> getUsers();
}
