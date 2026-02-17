package com.notionwidgets.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notionwidgets.ui.auth.LoginScreen
import com.notionwidgets.ui.database.DatabaseSelectScreen
import com.notionwidgets.ui.settings.SettingsScreen

object Routes {
    const val LOGIN = "login"
    const val DATABASE_SELECT = "database_select"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = {
                    navController.navigate(Routes.DATABASE_SELECT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DATABASE_SELECT) {
            DatabaseSelectScreen(
                onDatabaseSelected = {
                    navController.navigate(Routes.SETTINGS) {
                        popUpTo(Routes.DATABASE_SELECT) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChangeDatabaseClick = {
                    navController.navigate(Routes.DATABASE_SELECT)
                }
            )
        }
    }
}
