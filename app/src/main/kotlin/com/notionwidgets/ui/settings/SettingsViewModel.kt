package com.notionwidgets.ui.settings

import androidx.lifecycle.ViewModel
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val authMode: AuthMode = AuthMode.INTERNAL_TOKEN,
    val selectedDatabaseId: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            authMode = authManager.getAuthMode(),
            selectedDatabaseId = authManager.getSelectedDatabaseId(),
            isAuthenticated = authManager.isAuthenticated()
        )
    }

    fun logout() {
        authManager.clearAuth()
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }
}
