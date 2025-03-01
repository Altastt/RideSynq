package com.example.ridesynq.view.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ridesynq.viewmodel.AuthVM


fun NavGraphBuilder.settingsNavigation(navController: NavHostController, authViewModel: AuthVM) {
    navigation(
        route = GraphRoute.RIDE,
        startDestination = SettingsScreen.ProfileSettings.route
    ) {
        /*
        composable(SettingsScreen.ProfileSettings.route) {
            ProfileSettingsScreen(navController)
        }
        composable(SettingsScreen.Notification.route) {
            NotificationScreen(navController)
        }
        composable(SettingsScreen.Security.route) {
            SecurityScreen(navController)
        }

        authNavigation(navController, authViewModel)

         */
    }
}

sealed class SettingsScreen(val route: String) {
    object Notification : SettingsScreen("NOTIFICATION")
    object Security : SettingsScreen("SECURITY")
    object Language : SettingsScreen("LANGUAGE")
    object FAQ : SettingsScreen("FAQ")
    object ProfileSettings : SettingsScreen("PROFILESETTINGS")
}