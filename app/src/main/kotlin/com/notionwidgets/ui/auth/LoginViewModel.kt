package com.notionwidgets.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val authMode: AuthMode = AuthMode.INTERNAL_TOKEN,
    val tokenInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        if (authManager.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(authMode = mode, error = null)
    }

    fun setTokenInput(token: String) {
        _uiState.value = _uiState.value.copy(tokenInput = token, error = null)
    }

    fun submitInternalToken() {
        val token = _uiState.value.tokenInput.trim()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Token cannot be empty")
            return
        }
        authManager.saveAccessToken(token)
        authManager.saveAuthMode(AuthMode.INTERNAL_TOKEN)
        _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
    }

    fun getOAuthUrl(): String {
        return authManager.getOAuthAuthorizeUrl()
    }

    fun handleOAuthCallback(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authManager.exchangeOAuthCode(code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "OAuth failed"
                    )
                }
        }
    }
}
