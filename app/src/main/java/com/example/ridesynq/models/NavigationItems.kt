package com.example.ridesynq.models

import androidx.annotation.DrawableRes
import com.example.ridesynq.R

sealed class NavigationItems(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int
) {

    val baseRoute: String = route.substringBefore("/{").substringBefore("?")

    object Trip : NavigationItems(
        route = "trip_screen",
        title = "Поездки",
        icon = R.drawable.trip
    )

    object Search : NavigationItems(
        route = "search_screen",
        title = "Поиск",
        icon = R.drawable.search
    )


    object Profile : NavigationItems(
        route = "profile_screen",
        title = "Профиль",
        icon = R.drawable.profile
    )
}