package com.example.ridesynq.view.navigation

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ridesynq.models.NavigationItems
import com.example.ridesynq.view.ProfileScreen
import com.example.ridesynq.view.TripScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.sensors.internal.LastKnownLocation.getLastKnownLocation
import kotlinx.coroutines.launch


@Composable
fun MainNavigation(navController: NavHostController, onThemeUpdated: () -> Unit, authViewModel: AuthVM) {

    NavHost(navController, NavigationItems.Trip.route) {
        composable(NavigationItems.Trip.route) {
            TripScreen()
        }
        composable(NavigationItems.Search.route) {
            SearchScreen(authViewModel = authViewModel)
        }
        composable(NavigationItems.Chat.route) {
            ChatScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavigationItems.Profile.route) {
            ProfileScreen(navController, onThemeUpdated, authViewModel = authViewModel)
        }

        profileNavigation(navController)

        settingsNavigation(navController, authViewModel)

    }

}



@Composable
fun ChatScreen(navController: NavHostController, authViewModel: AuthVM) {
    Column {
        Text("Нет чатов", modifier = Modifier.padding(start = 165.dp, top = 100.dp))
    }
}

@Composable
fun SearchScreenNon(authViewModel: AuthVM) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    authViewModel: AuthVM,
    cameraPosition: CameraPosition = CameraPosition(
        Point(55.751574, 37.573856),
        11.0f,
        0.0f,
        0.0f
    )
) {

    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val scope = rememberCoroutineScope()
    var locationManager: LocationManager? by remember { mutableStateOf(null) }
    val locationListeners = remember { mutableListOf<LocationListener>() }

    // Запрос разрешения на геолокацию
    val permissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // Инициализация LocationManager
    LaunchedEffect(Unit) {
        locationManager = MapKitFactory.getInstance().createLocationManager()
    }

    // Обработчик для кнопки местоположения
    fun handleLocationClick() {
        if (permissionState.status.isGranted) {
            locationManager?.requestSingleUpdate(
                object : LocationListener {
                    override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                        mapView.map.move(
                            CameraPosition(
                                Point(location.position.latitude, location.position.longitude),
                                15f,
                                0f,
                                0f
                            )
                        )
                    }

                    override fun onLocationStatusUpdated(status: LocationStatus) {
                        // Обработка изменения статуса
                    }
                }
            )
        } else {
            permissionState.launchPermissionRequest()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = { handleLocationClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Мое местоположение"
            )
        }

        if (permissionState.status.shouldShowRationale) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Требуется доступ к геолокации") },
                text = { Text("Для определения вашего местоположения необходимо разрешение") },
                confirmButton = {
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
            locationListeners.forEach { listener ->
                locationManager?.unsubscribe(listener)
            }
            locationListeners.clear()
        }
    }
}




