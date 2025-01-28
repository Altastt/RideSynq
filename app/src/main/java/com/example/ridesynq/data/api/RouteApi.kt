package com.example.ridesynq.data.api

import com.example.ridesynq.data.model.Route
import retrofit2.http.GET

interface RouteApi {
    @GET("routes")
    suspend fun getRoutes(): List<Route>
}