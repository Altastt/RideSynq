package com.example.ridesynq.view.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ridesynq.R
import com.example.ridesynq.data.Company
import com.example.ridesynq.models.NavigationItems
import com.example.ridesynq.models.TimeSelectionDialog
import com.example.ridesynq.view.ProfileScreen
import com.example.ridesynq.view.TripScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.VehicleType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import java.time.LocalTime
import java.util.Calendar


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(
    navController: NavHostController,
    onThemeUpdated: () -> Unit,
    authViewModel: AuthVM
) {

    NavHost(navController, NavigationItems.Trip.route) {
        composable(NavigationItems.Trip.route) {
            TripScreen()
        }
        composable(NavigationItems.Search.route) {
            SearchScreen(
                authViewModel = authViewModel,
                Company(1, "АО \"БЕМТ\"", Point(55.607515, 37.646526))
            )
        }
        composable(NavigationItems.Profile.route) {
            ProfileScreen(navController, onThemeUpdated, authViewModel = authViewModel)
        }

        profileNavigation(navController)

        settingsNavigation(navController, authViewModel)

    }

}


@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    authViewModel: AuthVM,
    company: Company,
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
    var showDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var drivingSession by remember { mutableStateOf<DrivingSession?>(null) }
    var locationManager: LocationManager? by remember { mutableStateOf(null) }
    val locationListeners = remember { mutableListOf<LocationListener>() }

    // Инициализация маршрутизатора
    val drivingRouter = remember {
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
    }
    val routeCollection = remember { mapView.map.mapObjects.addCollection() }

    // Запрос разрешения на геолокацию
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // Инициализация LocationManager
    LaunchedEffect(Unit) {
        locationManager = MapKitFactory.getInstance().createLocationManager()

    }

    fun buildRoute(start: Point, end: Point, time: LocalTime) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
        }

        routeCollection.clear()

        val drivingOptions = DrivingOptions().apply {
            routesCount = 1
            avoidTolls = true
            departureTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedTime.hour)
                set(Calendar.MINUTE, selectedTime.minute)
            }.timeInMillis
        }

        val vehicleOptions = VehicleOptions().apply {
            vehicleType = VehicleType.DEFAULT
        }

        val points = listOf(
            RequestPoint(start, RequestPointType.WAYPOINT, null, null),
            RequestPoint(end, RequestPointType.WAYPOINT, null, null)
        )

        drivingSession = drivingRouter.requestRoutes(
            points,
            drivingOptions,
            vehicleOptions,
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                    routes.firstOrNull()?.let { route ->
                        routeCollection.addPolyline(route.geometry).apply {
                            strokeWidth = 5f
                            setStrokeColor(Color.BLUE)
                        }
                    }
                }

                override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                    Toast.makeText(context, "Ошибка: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    fun handleRouteConfirmation() {
        if (permissionState.status.isGranted) {
            MapKitFactory.getInstance().createLocationManager()
                .requestSingleUpdate(object : LocationListener {
                    override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                        val userLocation = Point(
                            location.position.latitude,
                            location.position.longitude
                        )
                        buildRoute(userLocation, company.location, time = LocalTime.now())
                        showTimePicker = true
                    }

                    override fun onLocationStatusUpdated(status: LocationStatus) {
                        // Обработка статуса
                    }
                })
        } else {
            permissionState.launchPermissionRequest()
        }
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
        // Функция для получения местоположения
        fun getCurrentLocation(callback: (Point?) -> Unit) {
            if (permissionState.status.isGranted) {
                locationManager?.requestSingleUpdate(object : LocationListener {
                    override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                        callback(Point(location.position.latitude, location.position.longitude))
                    }
                    override fun onLocationStatusUpdated(status: LocationStatus) {}
                })
            } else {
                permissionState.launchPermissionRequest()
                callback(null)
            }
        }

    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            // Кнопка построения маршрута
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.route),
                    contentDescription = "Построить маршрут",
                    modifier = Modifier.size(20.dp)
                )
            }
            //Кнопка текущего местоположения
            FloatingActionButton(
                onClick = { handleLocationClick() },
                modifier = Modifier
                    .padding(bottom = 100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Мое местоположение"
                )
            }
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

        // Диалог подтверждения
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Поездка на работу?") },
                dismissButton = {
                    Button(
                        modifier = Modifier.height(45.dp),
                        onClick = { showDialog = false }) {
                        Text("Нет", fontSize = 20.sp)
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.height(45.dp),
                        onClick = {
                        showDialog = false
                        handleRouteConfirmation()
                    }) {
                        Text("Да", fontSize = 20.sp)
                    }
                },
            )
        }

        // Диалог выбора времени
        if (showTimePicker) {
            TimeSelectionDialog(
                onTimeSelected = { time ->
                    selectedTime = time
                    showTimePicker = false
                    authViewModel.setDepartureTime(time)

                    // Добавить вызов построения маршрута
                    if (permissionState.status.isGranted) {
                        locationManager?.requestSingleUpdate(object : LocationListener {
                            override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                                val userLocation = Point(
                                    location.position.latitude,
                                    location.position.longitude
                                )
                                buildRoute(userLocation, company.location, selectedTime)
                            }
                            override fun onLocationStatusUpdated(status: LocationStatus) {}
                        })
                    }
                },
                onDismiss = { showTimePicker = false }
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




