package com.example.ridesynq

import android.app.Application
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

private lateinit var mapKit: MapKit
private lateinit var mapView: MapView

class RideSynqApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}