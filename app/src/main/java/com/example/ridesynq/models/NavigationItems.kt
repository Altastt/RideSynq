package com.example.ridesynq.models

import androidx.annotation.DrawableRes
import com.example.ridesynq.R

sealed class NavigationItems(
    val routeDefinition: String, // Полное определение маршрута, МОЖЕТ содержать плейсхолдеры
    val title: String,
    @DrawableRes val icon: Int
) {
    // Маршрут для прямого перехода (обычно без аргументов по умолчанию)
    // Используется в BottomNavigationBar и для начального старта графа
    val route: String = routeDefinition.substringBefore("/{").substringBefore("?")

    // Базовый маршрут (без плейсхолдеров и query-параметров) для deep links и сравнений
    val baseRoute: String = routeDefinition.substringBefore("/{").substringBefore("?")


    object Trip : NavigationItems(
        routeDefinition = "trip_screen", // route и baseRoute будут "trip_screen"
        title = "Поездки",
        icon = R.drawable.trip
    )

    object Search : NavigationItems(
        routeDefinition = "search_screen/{lat}/{lon}", // Для определения composable с аргументами
        title = "Поиск",
        icon = R.drawable.search
    ) {
        // route здесь будет "search_screen" - для BottomBar
        // baseRoute также будет "search_screen"

        const val LAT_ARG = "lat"
        const val LON_ARG = "lon"

        // НЕ ИСПОЛЬЗУЕТСЯ для navigate, т.к. есть routeDefinition для composable
        // Этот метод остается для создания строки, если нужно передать ее в deep link URI
        fun createRouteWithArgs(latitude: Double?, longitude: Double?): String {
            val latStr = latitude?.toString() ?: "null"
            val lonStr = longitude?.toString() ?: "null"
            // Возвращает строку, которую NavController сможет сопоставить с routeDefinition
            return "$baseRoute/$latStr/$lonStr" // "search_screen/55.0/37.0"
        }

        // Для создания строки URI для deep link
        fun createDeepLinkUriString(latitude: Double?, longitude: Double?): String {
            val latStr = latitude?.toString() ?: "null"
            val lonStr = longitude?.toString() ?: "null"
            return "app://ridesynq/$baseRoute/$latStr/$lonStr" // "app://ridesynq/search_screen/55.0/37.0"
        }
    }

    object Profile : NavigationItems(
        routeDefinition = "profile_screen", // route и baseRoute будут "profile_screen"
        title = "Профиль",
        icon = R.drawable.profile
    )
}