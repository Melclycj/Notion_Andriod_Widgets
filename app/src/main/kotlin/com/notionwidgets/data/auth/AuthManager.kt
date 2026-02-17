package com.notionwidgets.data.auth

interface AuthManager {
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun getAuthMode(): AuthMode
    fun saveAuthMode(mode: AuthMode)
    fun getSelectedDatabaseId(): String?
    fun saveSelectedDatabaseId(databaseId: String)
    fun isAuthenticated(): Boolean
    fun clearAuth()
    suspend fun exchangeOAuthCode(code: String): Result<String>
    fun getOAuthAuthorizeUrl(): String
}
