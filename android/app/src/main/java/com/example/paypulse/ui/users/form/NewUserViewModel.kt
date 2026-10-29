package com.example.paypulse.ui.users.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.R
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.user.EmailAlreadyExistsException
import com.example.paypulse.domain.user.FieldError
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

    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        firstName = value
        _uiState.update { it.copy(firstNameError = null, formError = null) }
    }

    fun onLastNameChange(value: String) {
        lastName = value
        _uiState.update { it.copy(lastNameError = null, formError = null) }
    }

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, formError = null) }
    }

    fun onPhoneChange(value: String) {
        phone = value
        _uiState.update { it.copy(phoneError = null, formError = null) }
    }

    fun onRoleChange(role: UserRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun onSaveClick() {
        if (_uiState.value.isSaving) return
        val draft = currentDraft()
        val errors = UserDraftValidator.validate(draft)
        _uiState.update { it.withFieldErrors(errors) }
        if (errors.isEmpty()) save(draft)
    }

    private fun currentDraft() = UserDraft(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        email = email.trim(),
        phone = UserDraftValidator.normalizePhone(phone),
        role = _uiState.value.role
    )

    private fun save(draft: UserDraft) {
        _uiState.update { it.copy(isSaving = true, formError = null) }
        viewModelScope.launch {
            try {
                repository.createUser(draft)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: EmailAlreadyExistsException) {
                _uiState.update { it.copy(isSaving = false, emailError = R.string.users_error_email_exists) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, formError = R.string.users_error_save_failed) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NewUserViewModel(AppContainer.userRepository) }
        }
    }
}

private fun UserFormUiState.withFieldErrors(errors: Map<UserField, FieldError>) = copy(
    firstNameError = errors[UserField.FIRST_NAME]?.toMessage(),
    lastNameError = errors[UserField.LAST_NAME]?.toMessage(),
    emailError = errors[UserField.EMAIL]?.toMessage(),
    phoneError = errors[UserField.PHONE]?.toMessage(),
    formError = null
)