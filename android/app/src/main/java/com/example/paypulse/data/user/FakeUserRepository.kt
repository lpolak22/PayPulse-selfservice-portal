package com.example.paypulse.data.user

import com.example.paypulse.domain.user.EmailAlreadyExistsException
import com.example.paypulse.domain.user.LastActiveAdminException
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserDraft
import com.example.paypulse.domain.user.UserNotFoundException
import com.example.paypulse.domain.user.UserRepository
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val FAKE_NETWORK_DELAY_MS = 500L

class FakeUserRepository : UserRepository {

    private val state = MutableStateFlow(sampleUsers)
    private val nextId = AtomicLong(sampleUsers.maxOf { it.id } + 1)

    override val users: Flow<List<User>> = state.map { list ->
        list.sortedWith(compareBy<User>({ it.lastName }, { it.firstName }))
    }

    override suspend fun createUser(draft: UserDraft): User {
        delay(FAKE_NETWORK_DELAY_MS)
        ensureEmailIsFree(draft.email, exceptId = null)
        val user = User(
            id = nextId.getAndIncrement(),
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            email = draft.email.trim(),
            phone = draft.phone?.trim()?.ifEmpty { null },
            role = draft.role,
            status = UserStatus.PENDING_ACTIVATION,
            createdAt = LocalDateTime.now()
        )
        state.update { it + user }
        return user
    }

    override suspend fun updateUser(id: Long, draft: UserDraft, status: UserStatus): User {
        delay(FAKE_NETWORK_DELAY_MS)
        val updated = findUser(id).copy(
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            email = draft.email.trim(),
            phone = draft.phone?.trim()?.ifEmpty { null },
            role = draft.role,
            status = status
        )
        ensureEmailIsFree(updated.email, exceptId = id)
        ensureActiveAdminRemains(state.value.map { if (it.id == id) updated else it })
        state.update { list -> list.map { if (it.id == id) updated else it } }
        return updated
    }

    override suspend fun deleteUser(id: Long) {
        delay(FAKE_NETWORK_DELAY_MS)
        findUser(id)
        ensureActiveAdminRemains(state.value.filterNot { it.id == id })
        state.update { list -> list.filterNot { it.id == id } }
    }

    private fun findUser(id: Long): User =
        state.value.firstOrNull { it.id == id } ?: throw UserNotFoundException()

    private fun ensureEmailIsFree(email: String, exceptId: Long?) {
        val taken = state.value.any { it.id != exceptId && it.email.equals(email.trim(), ignoreCase = true) }
        if (taken) throw EmailAlreadyExistsException()
    }

    private fun ensureActiveAdminRemains(users: List<User>) {
        val hasActiveAdmin = users.any { it.role == UserRole.ADMIN && it.status == UserStatus.ACTIVE }
        if (!hasActiveAdmin) throw LastActiveAdminException()
    }
}

private val sampleUsers = listOf(
    User(1, "Ana", "Horvat", "ana.horvat@trgovac.hr", "+385911234567", UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 0)),
    User(2, "Ivan", "Babić", "ivan.babic@trgovac.hr", "+385987654321", UserRole.USER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 3, 10, 30)),
    User(3, "Marija", "Kovač", "marija.kovac@trgovac.hr", null, UserRole.USER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 10, 14, 15)),
    User(4, "Tomislav", "Perić", "tomislav.peric@trgovac.hr", "+385951112233", UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 12, 8, 45)),
    User(5, "Lucija", "Jurić", "lucija.juric@trgovac.hr", null, UserRole.USER, UserStatus.INACTIVE, LocalDateTime.of(2026, 9, 20, 11, 0))
)