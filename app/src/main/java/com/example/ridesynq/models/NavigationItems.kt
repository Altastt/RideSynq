package com.example.ridesynq.models

import androidx.annotation.DrawableRes
import com.example.ridesynq.R

sealed class NavigationItems(var route: String, @DrawableRes var icon: Int, var title: String) {
    object Trip : NavigationItems("trip", R.drawable.trip, "Trip")
    object Search : NavigationItems("search/{lat}/{lon}", R.drawable.search, "Search") {
        const val LAT_ARG = "lat"
        const val LON_ARG = "lon"
        fun createRoute(latitude: Double?, longitude: Double?): String {
            val latArg = latitude?.toString() ?: "null"
            val lonArg = longitude?.toString() ?: "null"
            return "$baseRoute/$latArg/$lonArg"
        }

        fun createDeepLinkUriString(latitude: Double?, longitude: Double?): String {
            val latStr = latitude?.toString() ?: "null"
            val lonStr = longitude?.toString() ?: "null"
            return "app://ridesynq/$baseRoute/$latStr/$lonStr"
        }
    }
    object Profile : NavigationItems("profile", R.drawable.profile, "Profile")
    val baseRoute: String = route.substringBefore("/")

}
