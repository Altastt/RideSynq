package com.example.ridesynq.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ridesynq.ui.routes.RoutesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "routes") {
        composable("routes") {
            RoutesScreen()
        }
    }
}
