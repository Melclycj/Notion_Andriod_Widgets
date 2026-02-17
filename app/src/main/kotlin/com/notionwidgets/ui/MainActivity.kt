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
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        handleOAuthIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "notionwidgets" && uri.host == "auth") {
            val code = uri.getQueryParameter("code") ?: return
            // The LoginViewModel will handle this via the navigation state
            // For now, store the code so the ViewModel can pick it up
            intent.data = null
        }
    }
}
