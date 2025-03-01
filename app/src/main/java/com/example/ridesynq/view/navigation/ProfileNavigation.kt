package com.example.ridesynq.view.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation


fun NavGraphBuilder.profileNavigation(navController: NavHostController){
    navigation(
        route = GraphRoute.PROFILE,
        startDestination = ProfileScreenItems.Profile.route
    ){
        /*
        composable(ProfileScreenItems.Profile.route){
            ProfileScreen(navController)
        }
        composable(ProfileScreenItems.Subs.route){
            SubsScreen()
        }
         */
    }
}

sealed class ProfileScreenItems(val route: String) {
    object Profile : ProfileScreenItems("PROFILE")
    object Subs : ProfileScreenItems("SUBS")
}