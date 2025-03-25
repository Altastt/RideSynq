package com.example.ridesynq.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ridesynq.view.MainScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavigation(navController: NavHostController, onThemeUpdated: () -> Unit, authVM: AuthVM, companyVM: CompanyViewModel) {
    NavHost(
        navController = navController,
        route = GraphRoute.ROOT,
        startDestination = GraphRoute.AUTHENTICATION
    ) {
        composable(GraphRoute.MAIN){
            MainScreen(onThemeUpdated, authVM)
        }
        authNavigation(navController, authVM, companyVM)
    }
}

object GraphRoute {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "authentication_graph"
    const val MAIN = "main_graph"
    const val PROFILE = "profile_graph"
    const val RIDE = "ride_graph"
}