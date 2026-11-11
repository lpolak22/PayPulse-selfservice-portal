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
import com.example.paypulse.domain.auth.PasswordPolicy
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetRepository
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import com.example.paypulse.domain.passwordreset.ResetTokenCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val token: String,
    private val passwordResetRepository: PasswordResetRepository
) : ViewModel() {

    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    private var hasTriedToSubmit = false

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    init {
        checkLink()
    }

    fun onRetryClick() = checkLink()

    fun onLeave() = clearPasswords()

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update {
            it.copy(metRules = PasswordPolicy.metRules(value), confirmPasswordError = mismatchError(), formError = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        confirmPassword = value
        _uiState.update { it.copy(confirmPasswordError = mismatchError(), formError = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onSubmitClick() {
        if (_uiState.value.isLoading) return
        hasTriedToSubmit = true
        _uiState.update { it.copy(highlightUnmetRules = true, confirmPasswordError = mismatchError(), formError = null) }
        if (PasswordPolicy.isSatisfiedBy(password) && password == confirmPassword) submit()
    }

    private fun checkLink() {
        _uiState.update { it.copy(page = ResetPasswordPage.CheckingLink) }
        viewModelScope.launch {
            val page = when (val result = passwordResetRepository.checkToken(token)) {
                is ResetTokenCheckResult.Checked -> result.status.toPage()
                is ResetTokenCheckResult.Failure -> result.error.toLinkCheckProblem()
            }
            _uiState.update { it.copy(page = page) }
        }
    }

    private fun submit() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = passwordResetRepository.resetPassword(token, password, confirmPassword)) {
                PasswordResetResult.Success -> onReset()
                is PasswordResetResult.Failure -> onResetFailed(result.error)
            }
        }
    }

    private fun onReset() {
        clearPasswords()
        _uiState.update { it.copy(isLoading = false, isReset = true) }
    }

    private fun onResetFailed(error: PasswordResetError) {
        val linkProblem = error.toLinkProblemOrNull()
        if (linkProblem != null) clearPasswords()
        _uiState.update {
            when {
                linkProblem != null -> it.copy(isLoading = false, page = linkProblem)
                error == PasswordResetError.PASSWORD_MISMATCH -> it.copy(
                    isLoading = false,
                    confirmPasswordError = R.string.reset_password_error_mismatch
                )
                else -> it.copy(isLoading = false, formError = error.toResetFormMessage())
            }
        }
    }

    private fun clearPasswords() {
        password = ""
        confirmPassword = ""
    }

    @StringRes
    private fun mismatchError(): Int? =
        if (hasTriedToSubmit && password != confirmPassword) R.string.reset_password_error_mismatch else null

    companion object {
        fun factory(token: String) = viewModelFactory {
            initializer { ResetPasswordViewModel(token, AppContainer.passwordResetRepository) }
        }
    }
}
