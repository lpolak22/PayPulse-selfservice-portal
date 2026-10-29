package com.example.paypulse.domain.user

import java.time.LocalDateTime

enum class UserRole { ADMIN, USER }

enum class UserStatus { PENDING_ACTIVATION, ACTIVE, INACTIVE }

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: LocalDateTime
) {
    val fullName: String
        get() = "$firstName $lastName"

    val initials: String
        get() = listOfNotNull(firstName.firstOrNull(), lastName.firstOrNull())
            .joinToString("")
            .uppercase()
}

data class UserDraft(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: UserRole
)