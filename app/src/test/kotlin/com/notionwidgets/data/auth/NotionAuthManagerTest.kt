package com.notionwidgets.data.auth

import android.content.SharedPreferences
import com.notionwidgets.data.provider.notion.NotionApiService
import com.notionwidgets.data.provider.notion.NotionOAuthTokenResponse
import com.google.gson.JsonObject
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotionAuthManagerTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var apiService: NotionApiService
    private lateinit var authManager: NotionAuthManager

    private val storedStringValues = mutableMapOf<String, String?>()
    private val storedLongValues = mutableMapOf<String, Long>()

    @Before
    fun setUp() {
        editor = mockk(relaxed = true)
        every { editor.putString(any(), any()) } answers {
            storedStringValues[firstArg()] = secondArg()
            editor
        }
        every { editor.putLong(any(), any()) } answers {
            storedLongValues[firstArg()] = secondArg()
            editor
        }
        every { editor.remove(any()) } answers {
            storedStringValues.remove(firstArg())
            storedLongValues.remove(firstArg())
            editor
        }

        prefs = mockk()
        every { prefs.edit() } returns editor
        every { prefs.getString(any(), any()) } answers {
            storedStringValues[firstArg()] ?: secondArg()
        }
        every { prefs.getLong(any(), any()) } answers {
            storedLongValues[firstArg()] ?: secondArg()
        }

        apiService = mockk()
        authManager = NotionAuthManager(prefs, apiService)
    }

    @Test
    fun `getAccessToken returns null when not set`() {
        assertNull(authManager.getAccessToken())
    }

    @Test
    fun `saveAccessToken stores token`() {
        authManager.saveAccessToken("test-token")
        verify { editor.putString("notion_access_token", "test-token") }
    }

    @Test
    fun `getAccessToken returns saved token`() {
        storedStringValues["notion_access_token"] = "my-token"
        assertEquals("my-token", authManager.getAccessToken())
    }

    @Test
    fun `getAuthMode defaults to INTERNAL_TOKEN`() {
        assertEquals(AuthMode.INTERNAL_TOKEN, authManager.getAuthMode())
    }

    @Test
    fun `saveAuthMode stores mode`() {
        authManager.saveAuthMode(AuthMode.OAUTH)
        verify { editor.putString("notion_auth_mode", "OAUTH") }
    }

    @Test
    fun `getAuthMode returns OAUTH when saved`() {
        storedStringValues["notion_auth_mode"] = "OAUTH"
        assertEquals(AuthMode.OAUTH, authManager.getAuthMode())
    }

    @Test
    fun `getSelectedDatabaseId returns null when not set`() {
        assertNull(authManager.getSelectedDatabaseId())
    }

    @Test
    fun `saveSelectedDatabaseId stores id`() {
        authManager.saveSelectedDatabaseId("db-123")
        verify { editor.putString("notion_database_id", "db-123") }
    }

    @Test
    fun `isAuthenticated returns false when no token`() {
        assertFalse(authManager.isAuthenticated())
    }

    @Test
    fun `isAuthenticated returns true when token exists`() {
        storedStringValues["notion_access_token"] = "some-token"
        assertTrue(authManager.isAuthenticated())
    }

    @Test
    fun `clearAuth removes all keys`() {
        authManager.clearAuth()
        verify { editor.remove("notion_access_token") }
        verify { editor.remove("notion_auth_mode") }
        verify { editor.remove("notion_database_id") }
    }

    @Test
    fun `getOAuthAuthorizeUrl contains required parameters`() {
        val url = authManager.getOAuthAuthorizeUrl()
        assertTrue(url.startsWith("https://api.notion.com/v1/oauth/authorize"))
        assertTrue(url.contains("client_id="))
        assertTrue(url.contains("redirect_uri="))
        assertTrue(url.contains("response_type=code"))
        assertTrue(url.contains("owner=user"))
    }

    @Test
    fun `exchangeOAuthCode calls API and saves token`() = runTest {
        coEvery { apiService.exchangeToken(any()) } returns NotionOAuthTokenResponse(
            accessToken = "oauth-token-123",
            workspaceId = "ws-1",
            botId = "bot-1"
        )

        val result = authManager.exchangeOAuthCode("auth-code-456")

        assertTrue(result.isSuccess)
        assertEquals("oauth-token-123", result.getOrNull())
        verify { editor.putString("notion_access_token", "oauth-token-123") }
        verify { editor.putString("notion_auth_mode", "OAUTH") }
    }

    @Test
    fun `exchangeOAuthCode returns failure on API error`() = runTest {
        coEvery { apiService.exchangeToken(any()) } throws RuntimeException("Auth failed")

        val result = authManager.exchangeOAuthCode("bad-code")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getSyncIntervalMinutes returns default when not set`() {
        assertEquals(15L, authManager.getSyncIntervalMinutes())
    }

    @Test
    fun `saveSyncIntervalMinutes stores value`() {
        authManager.saveSyncIntervalMinutes(60L)
        verify { editor.putLong("notion_sync_interval", 60L) }
    }

    @Test
    fun `getSyncIntervalMinutes returns saved value`() {
        storedLongValues["notion_sync_interval"] = 30L
        assertEquals(30L, authManager.getSyncIntervalMinutes())
    }

    @Test
    fun `onTokenExpired removes access token`() {
        authManager.onTokenExpired()
        verify { editor.remove("notion_access_token") }
    }
}
