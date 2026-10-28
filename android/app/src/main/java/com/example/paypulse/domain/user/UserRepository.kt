package com.example.paypulse.domain.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val users: Flow<List<User>>
    suspend fun createUser(draft: UserDraft): User
    suspend fun updateUser(id: Long, draft: UserDraft, status: UserStatus): User
    suspend fun deleteUser(id: Long)
}