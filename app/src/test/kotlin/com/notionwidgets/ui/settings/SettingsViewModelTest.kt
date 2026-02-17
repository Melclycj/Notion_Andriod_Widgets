package com.notionwidgets.ui.settings

import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        authManager = mockk(relaxed = true)
    }

    @Test
    fun `init loads settings from auth manager`() {
        every { authManager.getAuthMode() } returns AuthMode.OAUTH
        every { authManager.getSelectedDatabaseId() } returns "db-123"
        every { authManager.isAuthenticated() } returns true

        val viewModel = SettingsViewModel(authManager)
        val state = viewModel.uiState.value

        assertEquals(AuthMode.OAUTH, state.authMode)
        assertEquals("db-123", state.selectedDatabaseId)
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun `init with internal token mode`() {
        every { authManager.getAuthMode() } returns AuthMode.INTERNAL_TOKEN
        every { authManager.getSelectedDatabaseId() } returns null
        every { authManager.isAuthenticated() } returns true

        val viewModel = SettingsViewModel(authManager)

        assertEquals(AuthMode.INTERNAL_TOKEN, viewModel.uiState.value.authMode)
    }

    @Test
    fun `logout clears auth and updates state`() {
        every { authManager.getAuthMode() } returns AuthMode.INTERNAL_TOKEN
        every { authManager.getSelectedDatabaseId() } returns "db-1"
        every { authManager.isAuthenticated() } returns true

        val viewModel = SettingsViewModel(authManager)
        assertTrue(viewModel.uiState.value.isAuthenticated)

        viewModel.logout()

        verify { authManager.clearAuth() }
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }
}
