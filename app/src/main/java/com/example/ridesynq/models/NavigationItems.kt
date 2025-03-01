package com.example.ridesynq.models

import com.example.ridesynq.R

sealed class NavigationItems(var route: String, var icon: Int, var title: String) {
    object Trip : NavigationItems("trip", R.drawable.trip, "Trip")
    object Search : NavigationItems("search", R.drawable.search, "Search")
    object Chat : NavigationItems("chat", R.drawable.chat, "Chat")
    object Profile : NavigationItems("profile", R.drawable.profile, "Profile")
}
