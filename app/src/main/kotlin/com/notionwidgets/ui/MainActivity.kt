package com.notionwidgets.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.ui.theme.NotionWidgetsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    private val _oauthCode = MutableStateFlow<String?>(null)
    val oauthCode: StateFlow<String?> = _oauthCode.asStateFlow()

    fun consumeOAuthCode(): String? {
        val code = _oauthCode.value
        _oauthCode.value = null
        return code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthIntent(intent)

        val startRoute = if (authManager.isAuthenticated()) {
            if (authManager.getSelectedDatabaseId() != null) {
                Routes.SETTINGS
            } else {
                Routes.DATABASE_SELECT
            }
        } else {
            Routes.LOGIN
        }

        setContent {
            NotionWidgetsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        startDestination = startRoute,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "notionwidgets" && uri.host == "auth") {
            val code = uri.getQueryParameter("code") ?: return
            _oauthCode.value = code
            intent.data = null
        }
    }
}
