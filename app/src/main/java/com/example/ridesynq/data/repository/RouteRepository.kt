package com.example.ridesynq.data.repository

import com.example.ridesynq.data.api.RouteApi
import com.example.ridesynq.data.model.Route
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val api: RouteApi
) {
    suspend fun getRoutes(): List<Route> = api.getRoutes()
}