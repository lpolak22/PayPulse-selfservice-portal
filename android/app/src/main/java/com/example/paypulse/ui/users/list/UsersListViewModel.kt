package com.example.paypulse.ui.users.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class UsersListViewModel(repository: UserRepository) : ViewModel() {

    var query by mutableStateOf("")
        private set

    val uiState: StateFlow<UsersListUiState> =
        combine(repository.users, snapshotFlow { query }) { users, term ->
            UsersListUiState(
                isLoading = false,
                users = users.filter { it.matches(term) },
                totalCount = users.size
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UsersListUiState()
        )

    fun onQueryChange(value: String) {
        query = value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { UsersListViewModel(AppContainer.userRepository) }
        }
    }
}

private fun User.matches(query: String): Boolean {
    val term = query.trim()
    return term.isEmpty() ||
            fullName.contains(term, ignoreCase = true) ||
            email.contains(term, ignoreCase = true)
}