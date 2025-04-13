package com.example.ridesynq.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier // << Импорт Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
            startDestination = NavigationItems.Trip.baseRoute
        ) {

            composable(NavigationItems.Trip.baseRoute) {
                TripScreen()
            }
            val searchArguments = listOf(
                navArgument(NavigationItems.Search.LAT_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "null"
                },
                navArgument(NavigationItems.Search.LON_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "null"
                }
            )
            val searchDeepLink = listOf(navDeepLink {
                uriPattern = "app://ridesynq/${NavigationItems.Search.baseRoute}/{${NavigationItems.Search.LAT_ARG}}/{${NavigationItems.Search.LON_ARG}}"
            })
            composable(
                NavigationItems.Search.route,
                arguments = searchArguments,
                deepLinks = searchDeepLink
            ) { backStackEntry ->

                val latitudeString = backStackEntry.arguments?.getString(NavigationItems.Search.LAT_ARG)
                val longitudeString = backStackEntry.arguments?.getString(NavigationItems.Search.LON_ARG)
                val initialLatitude = if (latitudeString == "null") null else latitudeString?.toDoubleOrNull()
                val initialLongitude = if (longitudeString == "null") null else longitudeString?.toDoubleOrNull()
                SearchScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM,
                    initialLatitude = initialLatitude,
                    initialLongitude = initialLongitude
                )
            }
            composable(NavigationItems.Profile.baseRoute) {
                ProfileScreen(
                    navController = navController,
                    onThemeUpdated = onThemeUpdated,
                    authViewModel = authVM
                )
            }

            // Вложенный граф для экранов настроек
            settingsNavigation(
                navController = navController,
                authViewModel = authVM,
                companyViewModel = companyVM,
                settingsGraphRoute = GraphRoute.SETTINGS
            )
        } // Конец графа GraphRoute.MAIN

        // Граф админа и т.д. можно добавить здесь
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
    companyViewModel: CompanyViewModel,
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
            CompanyScreen(navController, authViewModel, companyViewModel)
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