package com.scaffold.app.presentation.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaffold.app.domain.models.Credentials
import com.scaffold.app.domain.usecases.LoginUseCase
import com.scaffold.app.domain.usecases.ValidateEmailUseCase
import com.scaffold.app.domain.usecases.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed class LoginUiEvent {
    object NavigateToHome : LoginUiEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onRememberMeChange(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        val emailValid = validateEmail(state.email)
        val passwordValid = validatePassword(state.password)

        if (!emailValid) {
            _uiState.update { it.copy(emailError = "E-mail inválido") }
            return
        }
        if (!passwordValid) {
            _uiState.update { it.copy(passwordError = "Senha deve ter ao menos 8 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                loginUseCase(
                    Credentials(
                        email = state.email,
                        password = state.password,
                        rememberMe = state.rememberMe
                    )
                ).first()
                _uiEvent.emit(LoginUiEvent.NavigateToHome)
            } catch (e: Exception) {
                _uiState.update { it.copy(generalError = e.message ?: "Erro ao autenticar") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
