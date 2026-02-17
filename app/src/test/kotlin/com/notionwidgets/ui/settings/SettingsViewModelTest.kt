package com.notionwidgets.ui.settings

import android.content.Context
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import com.notionwidgets.widget.WidgetSyncWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var authManager: AuthManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        authManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        mockkObject(WidgetSyncWorker.Companion)
        every { WidgetSyncWorker.enqueue(any(), any()) } returns Unit
        every { WidgetSyncWorker.cancel(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkObject(WidgetSyncWorker.Companion)
    }

    @Test
    fun `init loads settings from auth manager`() {
        every { authManager.getAuthMode() } returns AuthMode.OAUTH
        every { authManager.getSelectedDatabaseId() } returns "db-123"
        every { authManager.isAuthenticated() } returns true
        every { authManager.getSyncIntervalMinutes() } returns 30L

        val viewModel = SettingsViewModel(authManager, context)
        val state = viewModel.uiState.value

        assertEquals(AuthMode.OAUTH, state.authMode)
        assertEquals("db-123", state.selectedDatabaseId)
        assertTrue(state.isAuthenticated)
        assertEquals(30L, state.syncIntervalMinutes)
    }

    @Test
    fun `init with internal token mode`() {
        every { authManager.getAuthMode() } returns AuthMode.INTERNAL_TOKEN
        every { authManager.getSelectedDatabaseId() } returns null
        every { authManager.isAuthenticated() } returns true
        every { authManager.getSyncIntervalMinutes() } returns 15L

        val viewModel = SettingsViewModel(authManager, context)

        assertEquals(AuthMode.INTERNAL_TOKEN, viewModel.uiState.value.authMode)
    }

    @Test
    fun `logout clears auth and updates state`() {
        every { authManager.getAuthMode() } returns AuthMode.INTERNAL_TOKEN
        every { authManager.getSelectedDatabaseId() } returns "db-1"
        every { authManager.isAuthenticated() } returns true
        every { authManager.getSyncIntervalMinutes() } returns 15L

        val viewModel = SettingsViewModel(authManager, context)
        assertTrue(viewModel.uiState.value.isAuthenticated)

        viewModel.logout()

        verify { authManager.clearAuth() }
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `setSyncInterval saves and updates state`() {
        every { authManager.getAuthMode() } returns AuthMode.INTERNAL_TOKEN
        every { authManager.getSelectedDatabaseId() } returns "db-1"
        every { authManager.isAuthenticated() } returns true
        every { authManager.getSyncIntervalMinutes() } returns 15L

        val viewModel = SettingsViewModel(authManager, context)
        viewModel.setSyncInterval(60L)

        verify { authManager.saveSyncIntervalMinutes(60L) }
        assertEquals(60L, viewModel.uiState.value.syncIntervalMinutes)
    }
}
