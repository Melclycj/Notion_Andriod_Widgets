package com.notionwidgets.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notionwidgets.data.auth.AuthMode
import com.notionwidgets.ui.MainActivity

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity

    LaunchedEffect(Unit) {
        val code = activity?.consumeOAuthCode()
        if (code != null) {
            viewModel.handleOAuthCallback(code)
        }
    }

    if (activity != null) {
        val pendingCode by activity.oauthCode.collectAsState()
        LaunchedEffect(pendingCode) {
            val code = activity.consumeOAuthCode()
            if (code != null) {
                viewModel.handleOAuthCallback(code)
            }
        }
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connect to Notion",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthModeSelector(
            selected = state.authMode,
            onSelect = viewModel::setAuthMode
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (state.authMode) {
            AuthMode.INTERNAL_TOKEN -> {
                OutlinedTextField(
                    value = state.tokenInput,
                    onValueChange = viewModel::setTokenInput,
                    label = { Text("Internal Integration Token") },
                    placeholder = { Text("ntn_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::submitInternalToken,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Connect")
                }
            }
            AuthMode.OAUTH -> {
                Button(
                    onClick = {
                        val url = viewModel.getOAuthUrl()
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Sign in with Notion")
                }
            }
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AuthModeSelector(
    selected: AuthMode,
    onSelect: (AuthMode) -> Unit
) {
    Column {
        AuthModeOption(
            label = "Internal Integration Token",
            description = "Paste your token from developers.notion.com",
            selected = selected == AuthMode.INTERNAL_TOKEN,
            onClick = { onSelect(AuthMode.INTERNAL_TOKEN) }
        )
        AuthModeOption(
            label = "OAuth Sign-in",
            description = "Sign in through Notion directly",
            selected = selected == AuthMode.OAUTH,
            onClick = { onSelect(AuthMode.OAUTH) }
        )
    }
}

@Composable
private fun AuthModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
