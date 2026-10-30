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
import com.example.paypulse.domain.user.UserStatus
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditUserViewModel(
    private val userId: Long,
    private val repository: UserRepository
) : ViewModel() {

    val input = UserFormInput()

    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState: StateFlow<EditUserUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onFieldChange(field: UserField, value: String) {
        input.update(field, value)
        updateForm { it.withoutError(field) }
    }

    fun onRoleChange(role: UserRole) = updateForm { it.copy(role = role) }

    fun onActiveChange(active: Boolean) {
        if (active) {
            _uiState.update { it.copy(status = UserStatus.ACTIVE) }
        } else {
            _uiState.update { it.copy(dialog = EditUserDialog.DEACTIVATE) }
        }
    }

    fun onDeleteClick() = _uiState.update { it.copy(dialog = EditUserDialog.DELETE) }

    fun onDialogDismiss() = _uiState.update { it.copy(dialog = null) }

    fun onDialogConfirm() {
        val dialog = _uiState.value.dialog
        _uiState.update { it.copy(dialog = null) }
        when (dialog) {
            EditUserDialog.DEACTIVATE -> _uiState.update { it.copy(status = UserStatus.INACTIVE) }
            EditUserDialog.DELETE -> delete()
            null -> Unit
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.form.isSaving) return
        val draft = input.toDraft(state.form.role)
        val errors = UserDraftValidator.validate(draft)
        updateForm { it.withFieldErrors(errors) }
        if (errors.isEmpty()) save(draft, state.status)
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val user = repository.getUser(userId)
                input.fill(user)
                _uiState.update {
                    it.copy(user = user, status = user.status, isLoading = false, form = it.form.copy(role = user.role))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun save(draft: UserDraft, status: UserStatus) {
        updateForm { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.updateUser(userId, draft, status)
                updateForm { it.copy(isSaving = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateForm { it.withSaveError(e) }
            }
        }
    }

    private fun delete() {
        updateForm { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.deleteUser(userId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateForm { it.withSaveError(e) }
            }
        }
    }

    private fun updateForm(transform: (UserFormUiState) -> UserFormUiState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    companion object {
        fun factory(userId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { EditUserViewModel(userId, AppContainer.userRepository) }
        }
    }
}