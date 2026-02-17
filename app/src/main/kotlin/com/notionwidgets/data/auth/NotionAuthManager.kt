package com.notionwidgets.data.auth

import android.content.SharedPreferences
import java.util.Base64
import com.notionwidgets.BuildConfig
import com.notionwidgets.data.provider.notion.NotionApiService
import com.google.gson.JsonObject
import javax.inject.Inject

class NotionAuthManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val apiService: NotionApiService
) : AuthManager {

    override fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override fun getAuthMode(): AuthMode {
        val mode = prefs.getString(KEY_AUTH_MODE, AuthMode.INTERNAL_TOKEN.name)
        return AuthMode.valueOf(mode ?: AuthMode.INTERNAL_TOKEN.name)
    }

    override fun saveAuthMode(mode: AuthMode) {
        prefs.edit().putString(KEY_AUTH_MODE, mode.name).apply()
    }

    override fun getSelectedDatabaseId(): String? {
        return prefs.getString(KEY_DATABASE_ID, null)
    }

    override fun saveSelectedDatabaseId(databaseId: String) {
        prefs.edit().putString(KEY_DATABASE_ID, databaseId).apply()
    }

    override fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    override fun clearAuth() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_AUTH_MODE)
            .remove(KEY_DATABASE_ID)
            .apply()
    }

    override fun getOAuthAuthorizeUrl(): String {
        val clientId = BuildConfig.NOTION_CLIENT_ID
        val redirectUri = BuildConfig.NOTION_REDIRECT_URI
        return "https://api.notion.com/v1/oauth/authorize" +
            "?client_id=$clientId" +
            "&redirect_uri=$redirectUri" +
            "&response_type=code" +
            "&owner=user"
    }

    /**
     * Exchange an OAuth authorization code for an access token.
     * Currently calls Notion directly (Option 1).
     * To add a server later, replace the API call target here.
     */
    override suspend fun exchangeOAuthCode(code: String): Result<String> {
        return runCatching {
            val credentials = "${BuildConfig.NOTION_CLIENT_ID}:${BuildConfig.NOTION_CLIENT_SECRET}"
            val basicAuth = Base64.getEncoder().encodeToString(
                credentials.toByteArray()
            )
            val body = JsonObject().apply {
                addProperty("grant_type", "authorization_code")
                addProperty("code", code)
                addProperty("redirect_uri", BuildConfig.NOTION_REDIRECT_URI)
            }
            val response = apiService.exchangeToken(body)
            val token = response.accessToken
            saveAccessToken(token)
            saveAuthMode(AuthMode.OAUTH)
            token
        }
    }

    override fun getSyncIntervalMinutes(): Long {
        return prefs.getLong(KEY_SYNC_INTERVAL, DEFAULT_SYNC_INTERVAL)
    }

    override fun saveSyncIntervalMinutes(minutes: Long) {
        prefs.edit().putLong(KEY_SYNC_INTERVAL, minutes).apply()
    }

    override fun onTokenExpired() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "notion_access_token"
        private const val KEY_AUTH_MODE = "notion_auth_mode"
        private const val KEY_DATABASE_ID = "notion_database_id"
        private const val KEY_SYNC_INTERVAL = "notion_sync_interval"
        const val DEFAULT_SYNC_INTERVAL = 15L
    }
}
