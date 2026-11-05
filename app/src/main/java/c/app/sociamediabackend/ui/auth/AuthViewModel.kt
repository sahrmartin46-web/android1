package c.app.sociamediabackend.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.model.AuthResponse
import c.app.sociamediabackend.data.model.LoginRequest
import c.app.sociamediabackend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val authResponse: AuthResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _loginState.value = LoginUiState.Error("Email required")
            return
        }
        if (password.isBlank()) {
            _loginState.value = LoginUiState.Error("Password required")
            return
        }

        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            when (val result = authRepository.login(LoginRequest(email, password))) {
                is NetworkResult.Success -> {
                    if (result.data.success) {
                        _loginState.value = LoginUiState.Success(result.data)
                    } else {
                        _loginState.value = LoginUiState.Error(
                            if (result.data.message.isNotBlank()) result.data.message else "Login failed"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _loginState.value = LoginUiState.Error(result.throwable.localizedMessage ?: "Network error")
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
