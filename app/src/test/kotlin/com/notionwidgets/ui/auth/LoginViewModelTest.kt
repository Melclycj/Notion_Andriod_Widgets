package com.notionwidgets.ui.auth

import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.auth.AuthMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var authManager: AuthManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authManager = mockk(relaxed = true)
        every { authManager.isAuthenticated() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not authenticated`() {
        val viewModel = LoginViewModel(authManager)
        val state = viewModel.uiState.value

        assertFalse(state.isAuthenticated)
        assertEquals(AuthMode.INTERNAL_TOKEN, state.authMode)
        assertEquals("", state.tokenInput)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `initial state detects existing auth`() {
        every { authManager.isAuthenticated() } returns true
        val viewModel = LoginViewModel(authManager)

        assertTrue(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `setAuthMode updates state`() {
        val viewModel = LoginViewModel(authManager)
        viewModel.setAuthMode(AuthMode.OAUTH)

        assertEquals(AuthMode.OAUTH, viewModel.uiState.value.authMode)
    }

    @Test
    fun `setTokenInput updates state`() {
        val viewModel = LoginViewModel(authManager)
        viewModel.setTokenInput("ntn_abc123")

        assertEquals("ntn_abc123", viewModel.uiState.value.tokenInput)
    }

    @Test
    fun `submitInternalToken with blank token shows error`() {
        val viewModel = LoginViewModel(authManager)
        viewModel.setTokenInput("   ")
        viewModel.submitInternalToken()

        assertEquals("Token cannot be empty", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `submitInternalToken saves token and authenticates`() {
        val viewModel = LoginViewModel(authManager)
        viewModel.setTokenInput("ntn_valid_token")
        viewModel.submitInternalToken()

        verify { authManager.saveAccessToken("ntn_valid_token") }
        verify { authManager.saveAuthMode(AuthMode.INTERNAL_TOKEN) }
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getOAuthUrl delegates to authManager`() {
        every { authManager.getOAuthAuthorizeUrl() } returns "https://example.com/auth"
        val viewModel = LoginViewModel(authManager)

        assertEquals("https://example.com/auth", viewModel.getOAuthUrl())
    }

    @Test
    fun `handleOAuthCallback success authenticates`() = runTest {
        coEvery { authManager.exchangeOAuthCode("test-code") } returns Result.success("token")
        val viewModel = LoginViewModel(authManager)

        viewModel.handleOAuthCallback("test-code")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `handleOAuthCallback failure shows error`() = runTest {
        coEvery { authManager.exchangeOAuthCode("bad-code") } returns Result.failure(Exception("Invalid code"))
        val viewModel = LoginViewModel(authManager)

        viewModel.handleOAuthCallback("bad-code")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("Invalid code", viewModel.uiState.value.error)
    }

    @Test
    fun `setAuthMode clears error`() {
        val viewModel = LoginViewModel(authManager)
        viewModel.setTokenInput("")
        viewModel.submitInternalToken()
        assertEquals("Token cannot be empty", viewModel.uiState.value.error)

        viewModel.setAuthMode(AuthMode.OAUTH)
        assertNull(viewModel.uiState.value.error)
    }
}
