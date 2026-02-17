package com.notionwidgets.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import com.notionwidgets.widget.WidgetSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val authMode: AuthMode = AuthMode.INTERNAL_TOKEN,
    val selectedDatabaseId: String? = null,
    val isAuthenticated: Boolean = false,
    val syncIntervalMinutes: Long = 15L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    @ApplicationContext private val context: Context
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
            isAuthenticated = authManager.isAuthenticated(),
            syncIntervalMinutes = authManager.getSyncIntervalMinutes()
        )
    }

    fun setSyncInterval(minutes: Long) {
        authManager.saveSyncIntervalMinutes(minutes)
        _uiState.value = _uiState.value.copy(syncIntervalMinutes = minutes)
        WidgetSyncWorker.enqueue(context, minutes)
    }

    fun logout() {
        authManager.clearAuth()
        WidgetSyncWorker.cancel(context)
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }

    companion object {
        val SYNC_INTERVAL_OPTIONS = listOf(15L, 30L, 60L, 120L)
    }
}
