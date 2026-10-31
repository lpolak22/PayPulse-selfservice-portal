package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.AppUserDto
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import java.time.LocalDateTime

fun AppUserDto.toUser(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = UserRole.valueOf(role),
    status = UserStatus.valueOf(status),
    createdAt = LocalDateTime.parse(createdAt)
)
