package com.notionwidgets.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.provider.NotionDatabase
import com.notionwidgets.data.provider.TaskProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatabaseSelectUiState(
    val databases: List<NotionDatabase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedId: String? = null
)

@HiltViewModel
class DatabaseSelectViewModel @Inject constructor(
    private val taskProvider: TaskProvider,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatabaseSelectUiState())
    val uiState: StateFlow<DatabaseSelectUiState> = _uiState.asStateFlow()

    init {
        loadDatabases()
    }

    fun loadDatabases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            taskProvider.searchDatabases()
                .onSuccess { databases ->
                    _uiState.value = _uiState.value.copy(
                        databases = databases,
                        isLoading = false,
                        selectedId = authManager.getSelectedDatabaseId()
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load databases"
                    )
                }
        }
    }

    fun selectDatabase(databaseId: String) {
        authManager.saveSelectedDatabaseId(databaseId)
        _uiState.value = _uiState.value.copy(selectedId = databaseId)
    }
}
