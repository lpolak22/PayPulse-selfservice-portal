package com.example.paypulse.ui.auth.activation

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paypulse.R
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationRepository
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.TokenCheckResult
import com.example.paypulse.domain.auth.PasswordPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivationViewModel(
    private val token: String,
    private val activationRepository: ActivationRepository
) : ViewModel() {

    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    private var hasTriedToSubmit = false

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        checkLink()
    }

    fun onRetryClick() = checkLink()

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
        _uiState.update { it.copy(page = ActivationPage.CheckingLink) }
        viewModelScope.launch {
            val page = when (val result = activationRepository.checkToken(token)) {
                is TokenCheckResult.Checked -> result.status.toPage()
                is TokenCheckResult.Failure -> result.error.toLinkCheckProblem()
            }
            _uiState.update { it.copy(page = page) }
        }
    }

    private fun submit() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = activationRepository.activate(token, password, confirmPassword)) {
                ActivationResult.Success -> onActivated()
                is ActivationResult.Failure -> onActivationFailed(result.error)
            }
        }
    }

    private fun onActivated() {
        clearPasswords()
        _uiState.update { it.copy(isLoading = false, isActivated = true) }
    }

    private fun onActivationFailed(error: ActivationError) {
        val linkProblem = error.toLinkProblemOrNull()
        if (linkProblem != null) clearPasswords()
        _uiState.update {
            when {
                linkProblem != null -> it.copy(isLoading = false, page = linkProblem)
                error == ActivationError.PASSWORD_MISMATCH -> it.copy(
                    isLoading = false,
                    confirmPasswordError = R.string.activation_error_mismatch
                )
                else -> it.copy(isLoading = false, formError = error.toFormMessage())
            }
        }
    }

    private fun clearPasswords() {
        password = ""
        confirmPassword = ""
    }

    @StringRes
    private fun mismatchError(): Int? =
        if (hasTriedToSubmit && password != confirmPassword) R.string.activation_error_mismatch else null
}
