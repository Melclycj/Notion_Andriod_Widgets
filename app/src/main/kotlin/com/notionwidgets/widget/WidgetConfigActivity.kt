package com.notionwidgets.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.ui.database.DatabaseSelectScreen
import com.notionwidgets.ui.theme.NotionWidgetsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        if (!authManager.isAuthenticated()) {
            finish()
            return
        }

        setContent {
            NotionWidgetsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DatabaseSelectScreen(
                        onDatabaseSelected = { confirmWidget() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun confirmWidget() {
        lifecycleScope.launch {
            val glanceManager = GlanceAppWidgetManager(this@WidgetConfigActivity)
            val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
            TodoGlanceWidget().update(this@WidgetConfigActivity, glanceId)

            val resultValue = Intent().putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId
            )
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
