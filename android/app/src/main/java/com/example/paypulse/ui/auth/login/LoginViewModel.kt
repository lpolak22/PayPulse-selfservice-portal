package com.example.paypulse.ui.auth.login

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
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.EmailRule
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = null, formError = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        if (_uiState.value.isLoading) return
        val emailError = emailError(email)
        val passwordError = if (password.isBlank()) R.string.login_error_password_required else null
        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, formError = null) }
        if (emailError == null && passwordError == null) login()
    }

    private fun login() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                LoginResult.Success -> _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                is LoginResult.Failure -> onLoginFailed(result.error)
            }
        }
    }

    private fun onLoginFailed(error: LoginError) {
        if (error == LoginError.INVALID_CREDENTIALS) password = ""
        _uiState.update { it.copy(isLoading = false, isPasswordVisible = false, formError = error.toMessage()) }
    }

    @StringRes
    private fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.login_error_email_required
        !EmailRule.isValid(email.trim()) -> R.string.login_error_email_invalid
        else -> null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(AppContainer.authRepository) }
        }
    }
}
