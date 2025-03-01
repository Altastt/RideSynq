package com.example.ridesynq.view.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ridesynq.view.authScreens.ForgotScreen
import com.example.ridesynq.view.authScreens.LoginScreen
import com.example.ridesynq.view.authScreens.RegistrationScreen
import com.example.ridesynq.viewmodel.AuthVM


fun NavGraphBuilder.authNavigation(navController: NavHostController, authViewModel: AuthVM) {
    navigation(
        route = GraphRoute.AUTHENTICATION,
        startDestination = AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(AuthScreen.Registration.route) {
            RegistrationScreen(navController, authViewModel)
        }
        composable(AuthScreen.Forgot.route) {
            ForgotScreen(navController, authViewModel)
        }
    }
}

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("LOGIN")
    object Registration : AuthScreen("REGISTRATION")
    object Forgot : AuthScreen("FORGOT")
}