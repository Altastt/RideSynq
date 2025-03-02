package com.example.ridesynq

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory


class RideSynqApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        DirectionsFactory.initialize(this)
    }
}