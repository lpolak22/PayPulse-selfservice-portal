package com.example.paypulse.ui.users.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.user.UserDraft
import com.example.paypulse.domain.user.UserDraftValidator
import com.example.paypulse.domain.user.UserField
import com.example.paypulse.domain.user.UserRepository
import com.example.paypulse.domain.user.UserRole
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewUserViewModel(private val repository: UserRepository) : ViewModel() {

    val input = UserFormInput()

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    fun onFieldChange(field: UserField, value: String) {
        input.update(field, value)
        _uiState.update { it.withoutError(field) }
    }

    fun onRoleChange(role: UserRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun onSaveClick() {
        if (_uiState.value.isSaving) return
        val draft = input.toDraft(_uiState.value.role)
        val errors = UserDraftValidator.validate(draft)
        _uiState.update { it.withFieldErrors(errors) }
        if (errors.isEmpty()) save(draft)
    }

    private fun save(draft: UserDraft) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.createUser(draft)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.withSaveError(e) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NewUserViewModel(AppContainer.userRepository) }
        }
    }
}