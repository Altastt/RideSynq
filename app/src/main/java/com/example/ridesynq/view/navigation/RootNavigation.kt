package com.example.ridesynq.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
// NavType, navArgument, navDeepLink are no longer needed here for SearchScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ridesynq.models.NavigationItems
import com.example.ridesynq.view.AddCarScreen
import com.example.ridesynq.view.ChangeEmailScreen
import com.example.ridesynq.view.ChangePasswordScreen
import com.example.ridesynq.view.CompanyScreen
import com.example.ridesynq.view.EditProfileScreen
import com.example.ridesynq.view.ProfileScreen
import com.example.ridesynq.view.ProfileSettingsScreen
import com.example.ridesynq.view.SearchScreen
import com.example.ridesynq.view.TripScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onThemeUpdated: () -> Unit,
    authVM: AuthVM,
    companyVM: CompanyViewModel
) {
    NavHost(
        navController = navController,
        route = GraphRoute.ROOT,
        startDestination = GraphRoute.AUTHENTICATION,
        modifier = modifier
    ) {

        authNavigation(navController, authVM, companyVM)

        navigation(
            route = GraphRoute.MAIN,
            startDestination = NavigationItems.Trip.route // Use simple route
        ) {

            composable(NavigationItems.Trip.route) { // Use simple route
                TripScreen(navController = navController, authViewModel = authVM) // Pass NavController if TripScreen needs it
            }

            // --- Simplified SearchScreen Composable ---
            composable(NavigationItems.Search.route) { // Use simple route
                SearchScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM // Pass down the shared VM
                )
            }
            // -----------------------------------------

            composable(NavigationItems.Profile.route) { // Use simple route
                ProfileScreen(
                    navController = navController,
                    onThemeUpdated = onThemeUpdated,
                    authViewModel = authVM
                )
            }

            settingsNavigation(
                navController = navController,
                authViewModel = authVM,
                companyViewModel = companyVM, // Pass shared VM
                settingsGraphRoute = GraphRoute.SETTINGS
            )
        } // Конец графа GraphRoute.MAIN
    } // Конец корневого NavHost
}

object GraphRoute {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "authentication_graph"
    const val MAIN = "main_graph"
    const val SETTINGS = "settings_graph"
    const val ADMIN_MAIN = "admin_main_graph"
}

fun NavGraphBuilder.settingsNavigation(
    navController: NavHostController,
    authViewModel: AuthVM,
    companyViewModel: CompanyViewModel, // Accept shared VM
    settingsGraphRoute: String
) {
    navigation(
        route = settingsGraphRoute,
        startDestination = SettingsScreen.ProfileSettings.route
    ) {
        composable(SettingsScreen.ProfileSettings.route) {
            ProfileSettingsScreen(navController)
        }
        composable(SettingsScreen.AddCar.route) {
            AddCarScreen(navController, authViewModel)
        }
        composable(SettingsScreen.RCompany.route) {
            CompanyScreen(navController, authViewModel, companyViewModel) // Pass it here
        }
        composable(SettingsScreen.EditProfile.route) {
            EditProfileScreen(navController, authViewModel)
        }
        composable(SettingsScreen.ChangeEmail.route) {
            ChangeEmailScreen(navController, authViewModel)
        }
        composable(SettingsScreen.ChangePassword.route) {
            ChangePasswordScreen(navController, authViewModel)
        }
    }
}

sealed class SettingsScreen(val route: String) {
    object AddCar : SettingsScreen("ADDCAR")
    object RCompany : SettingsScreen("COMPANY")
    object ProfileSettings : SettingsScreen("PROFILESETTINGS")
    object EditProfile : SettingsScreen("EDIT_PROFILE")
    object ChangeEmail : SettingsScreen("CHANGE_EMAIL")
    object ChangePassword : SettingsScreen("CHANGE_PASSWORD")
}