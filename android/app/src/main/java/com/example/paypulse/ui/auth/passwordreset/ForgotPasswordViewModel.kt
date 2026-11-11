package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.R
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.auth.EmailRule
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetRepository
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val passwordResetRepository: PasswordResetRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, formError = null) }
    }

    fun onSubmitClick() {
        if (_uiState.value.isLoading) return
        val emailError = emailError(email)
        _uiState.update { it.copy(emailError = emailError, formError = null) }
        if (emailError == null) submit()
    }

    private fun submit() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = passwordResetRepository.requestReset(email.trim())) {
                PasswordResetResult.Success -> _uiState.update { it.copy(isLoading = false, isSent = true) }
                is PasswordResetResult.Failure -> onRequestFailed(result.error)
            }
        }
    }

    private fun onRequestFailed(error: PasswordResetError) {
        _uiState.update {
            if (error == PasswordResetError.INVALID_EMAIL) {
                it.copy(isLoading = false, emailError = R.string.forgot_password_error_email_invalid)
            } else {
                it.copy(isLoading = false, formError = error.toForgotPasswordMessage())
            }
        }
    }

    @StringRes
    private fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.forgot_password_error_email_required
        !EmailRule.isValid(email.trim()) -> R.string.forgot_password_error_email_invalid
        else -> null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ForgotPasswordViewModel(AppContainer.passwordResetRepository) }
        }
    }
}
