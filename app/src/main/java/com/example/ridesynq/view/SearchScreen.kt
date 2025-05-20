package com.example.ridesynq.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color // Импорт для Color.BLUE
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// import androidx.compose.ui.viewinterop.AndroidViewBinding // Если не используется MapViewBinding, можно убрать
// import androidx.core.content.ContextCompat // Если не используется для цвета, можно убрать
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesynq.R
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.entities.Company
// import com.example.ridesynq.databinding.MapViewBinding // Если не используется, можно убрать
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel
import com.example.ridesynq.viewmodel.TripVMFactory
import com.example.ridesynq.viewmodel.TripViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.logo.Alignment as LogoAlignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import java.util.*

enum class TripDirection { TO_WORK, FROM_WORK }

fun parseCoordinate(input: String, range: Double): Double? {
    val value = input.replace(',', '.').toDoubleOrNull()
    return if (value != null && value in -range..range) value else null
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    authViewModel: AuthVM,
    companyViewModel: CompanyViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val localFocusManager = LocalFocusManager.current

    val appDatabase = AppDatabase.getDatabase(context)
    val tripViewModel: TripViewModel = viewModel(
        factory = TripVMFactory(appDatabase.tripDao(), appDatabase.userTripDao(), authViewModel)
    )

    val targetCoordsFromVM by companyViewModel.mapTargetCoordinates.collectAsState()
    val allCompaniesState by companyViewModel.allCompanies.collectAsState(initial = emptyList())
    val userCompanyData: Company? by remember(currentUser, allCompaniesState) {
        derivedStateOf {
            currentUser?.let { user ->
                allCompaniesState.find { it.id == user.company_id }
            }
        }
    }

    var tripDirection by remember { mutableStateOf(TripDirection.TO_WORK) }
    var inputLatitudeStr by remember { mutableStateOf("") }
    var inputLongitudeStr by remember { mutableStateOf("") }
    var isInputLatValid by remember { mutableStateOf(true) }
    var isInputLonValid by remember { mutableStateOf(true) }

    var startPointState by remember { mutableStateOf<Point?>(null) }
    var endPointState by remember { mutableStateOf<Point?>(null) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }

    val mapView = remember { MapView(context) }
    var mapObjectCollectionState by remember { mutableStateOf<MapObjectCollection?>(null) }
    var drivingRouterState by remember { mutableStateOf<DrivingRouter?>(null) }
    var drivingRoutePolylineState by remember { mutableStateOf<PolylineMapObject?>(null) }
    var currentDrivingSessionState by remember { mutableStateOf<DrivingSession?>(null) }
    var companyMarkerState by remember { mutableStateOf<PlacemarkMapObject?>(null) }
    var startPlacemarkState by remember { mutableStateOf<PlacemarkMapObject?>(null) }
    var endPlacemarkState by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    var yandexLocationManager: LocationManager? by remember { mutableStateOf(null) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var initialCenteringDone by remember { mutableStateOf(false) }


    // Инициализация MapKit объектов
    LaunchedEffect(Unit) {
        mapObjectCollectionState = mapView.map.mapObjects.addCollection()
        drivingRouterState = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
        mapView.map.logo.setAlignment(LogoAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM))
        yandexLocationManager = MapKitFactory.getInstance().createLocationManager()
    }

    // Начальное центрирование карты
    LaunchedEffect(mapView, targetCoordsFromVM, userCompanyData, mapObjectCollectionState) {
        if (initialCenteringDone && targetCoordsFromVM == null) return@LaunchedEffect

        val map = mapView.map
        val mapObjects = mapObjectCollectionState ?: return@LaunchedEffect
        companyMarkerState?.let { mapObjects.remove(it); companyMarkerState = null }

        var pointToCenter: Point? = null
        var zoom = 11.0f
        var setCompanyMarker = false

        if (targetCoordsFromVM != null) {
            pointToCenter = Point(targetCoordsFromVM!!.first, targetCoordsFromVM!!.second)
            zoom = 15.5f; setCompanyMarker = true; companyViewModel.consumeMapTarget()
            Log.d("SearchScreen", "Centering on target from VM: $pointToCenter")
            initialCenteringDone = true
        } else if (userCompanyData != null && !initialCenteringDone) {
            pointToCenter = Point(userCompanyData!!.latitude, userCompanyData!!.longitude)
            zoom = 14.5f; setCompanyMarker = true
            Log.d("SearchScreen", "Initial centering on user's company: $pointToCenter")
            initialCenteringDone = true
        } else if (!initialCenteringDone) {
            pointToCenter = Point(55.751574, 37.573856); zoom = 10.0f // Москва
            Log.d("SearchScreen", "Initial centering: Default (Moscow) $pointToCenter")
            initialCenteringDone = true
        }

        pointToCenter?.let {
            map.move(CameraPosition(it, zoom, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 0.8f), null)
            if (setCompanyMarker) {
                companyMarkerState = mapObjects.addPlacemark(it, ImageProvider.fromResource(context, R.drawable.placemark_company)).apply { zIndex = 10f }
            }
        }
    }

    // Обновление точек старта/конца при смене направления или данных компании
    LaunchedEffect(tripDirection, userCompanyData) {
        currentDrivingSessionState?.cancel()
        drivingRoutePolylineState?.let { mapObjectCollectionState?.remove(it); drivingRoutePolylineState = null }
        startPlacemarkState?.let { mapObjectCollectionState?.remove(it); startPlacemarkState = null }
        endPlacemarkState?.let { mapObjectCollectionState?.remove(it); endPlacemarkState = null }

        if (userCompanyData == null) {
            startPointState = null; endPointState = null
            inputLatitudeStr = ""; inputLongitudeStr = ""
            return@LaunchedEffect
        }
        when (tripDirection) {
            TripDirection.TO_WORK -> {
                startPointState = null
                endPointState = Point(userCompanyData!!.latitude, userCompanyData!!.longitude)
            }
            TripDirection.FROM_WORK -> {
                startPointState = Point(userCompanyData!!.latitude, userCompanyData!!.longitude)
                endPointState = null
            }
        }
        inputLatitudeStr = ""; inputLongitudeStr = ""
        isInputLatValid = true; isInputLonValid = true
    }

    fun setMapPoint(point: Point, moveMap: Boolean, zoomLevel: Float = 16.0f) {
        if (tripDirection == TripDirection.TO_WORK) {
            startPointState = point
        } else {
            endPointState = point
        }
        if (moveMap) {
            mapView.map.move(CameraPosition(point, zoomLevel, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 0.6f), null)
        }
    }
    fun createScaledImageProvider(
        context: Context,
        @DrawableRes resId: Int,
        targetWidthDp: Int,
        targetHeightDp: Int
    ): ImageProvider {
        val density = context.resources.displayMetrics.density
        val targetWidthPx = (targetWidthDp * density).toInt()
        val targetHeightPx = (targetHeightDp * density).toInt()


        val originalBitmap = BitmapFactory.decodeResource(context.resources, resId)


        if (targetWidthPx <= 0 || targetHeightPx <= 0 || originalBitmap == null) {

            Log.e("ImageScaling", "Failed to scale image or invalid dimensions for resId: $resId. Using original.")
            return ImageProvider.fromResource(context, resId)
        }

        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, true)
        return ImageProvider.fromBitmap(scaledBitmap)
    }

    // Обновление меток старта и конца
    LaunchedEffect(startPointState, endPointState, mapObjectCollectionState, companyMarkerState) {
        val mapObjects = mapObjectCollectionState ?: return@LaunchedEffect

        // Удаляем старую метку старта
        startPlacemarkState?.let { mapObjects.remove(it); startPlacemarkState = null }
        // Добавляем новую метку старта с масштабированной иконкой
        startPointState?.let {
            if (it != companyMarkerState?.geometry) {
                startPlacemarkState = mapObjects.addPlacemark(it).apply {
                    setIcon(createScaledImageProvider(context, R.drawable.placemark_start, 30, 30))
                    zIndex = 20f
                }
            }
        }


        endPlacemarkState?.let { mapObjects.remove(it); endPlacemarkState = null }
        endPointState?.let {
            if (it != companyMarkerState?.geometry) { // Не добавляем метку конца, если она совпадает с меткой компании
                endPlacemarkState = mapObjects.addPlacemark(it).apply {
                    setIcon(createScaledImageProvider(context, R.drawable.placemark_end, 30, 30))
                    zIndex = 20f
                }
            }
        }
    }

    val drivingSessionListener = remember {
        object : DrivingSession.DrivingRouteListener {
            override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                Log.d("SearchScreen", "Received ${routes.size} routes")
                drivingRoutePolylineState?.let { mapObjectCollectionState?.remove(it) } // Удаляем старый маршрут

                routes.firstOrNull()?.let { route ->
                    Log.d("SearchScreen", "Route geometry: ${route.geometry.points.size} points")
                    drivingRoutePolylineState = mapObjectCollectionState?.addPolyline(route.geometry)?.apply {
                        strokeWidth = 5f
                        setStrokeColor(Color.BLUE) // Как в старой реализации
                        zIndex = 5f // zIndex важен для порядка отрисовки
                    }
                    Log.d("SearchScreen", "Route polyline added to map.")
                } ?: run {
                    Log.d("SearchScreen", "No routes received.")
                    drivingRoutePolylineState = null // Если маршрутов нет, обнуляем
                }
            }

            override fun onDrivingRoutesError(error: Error) {
                val errorMessage = when (error) {
                    is NetworkError -> "Ошибка сети при построении маршрута"
                    is RemoteError -> "Ошибка сервера при построении маршрута"
                    else -> "Неизвестная ошибка построения маршрута: ${error.javaClass.simpleName}"
                }
                Log.e("SearchScreen", "Route error: $errorMessage")
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                drivingRoutePolylineState = null // Обнуляем в случае ошибки
            }
        }
    }

    LaunchedEffect(startPointState, endPointState, selectedDateTime) {
        val router = drivingRouterState ?: return@LaunchedEffect

        if (startPointState == null || endPointState == null) {
            currentDrivingSessionState?.cancel()
            drivingRoutePolylineState?.let {
                mapObjectCollectionState?.remove(it)
                drivingRoutePolylineState = null
            }
            Log.d("SearchScreenRoute", "Route calculation skipped: start or end point is null. Cleared old route.")
            return@LaunchedEffect
        }

        // Очистка старого маршрута перед запросом нового
        currentDrivingSessionState?.cancel()
        drivingRoutePolylineState?.let {
            mapObjectCollectionState?.remove(it)
            drivingRoutePolylineState = null
        }

        Log.d("SearchScreenRoute", "Starting route calculation from: $startPointState to: $endPointState with time: ${selectedDateTime.time}")

        val drivingOptions = DrivingOptions().apply {
            routesCount = 1
            avoidTolls = true
            departureTime = selectedDateTime.timeInMillis // Используем выбранное время
        }
        val vehicleOptions = VehicleOptions() // vehicleType = VehicleType.DEFAULT по умолчанию

        val requestPoints = listOf(
            RequestPoint(startPointState!!, RequestPointType.WAYPOINT, null, null),
            RequestPoint(endPointState!!, RequestPointType.WAYPOINT, null, null)
        )

        currentDrivingSessionState = router.requestRoutes(
            requestPoints,
            drivingOptions,
            vehicleOptions,
            drivingSessionListener
        )
    }

    var showRoleDialog by remember { mutableStateOf(false) }
    var isDriverRole by remember { mutableStateOf(true) }
    var seatsInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    DisposableEffect(Unit) {
        mapView.onStart()
        onDispose { mapView.onStop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .padding(bottom = 80.dp) // Оставляем, если это важно для вашего UI
                .onGloballyPositioned { layoutCoordinates ->
                    val width = layoutCoordinates.size.width
                    val height = layoutCoordinates.size.height
                    mapView.mapWindow.focusRect = ScreenRect(
                        ScreenPoint(0f, 0f),
                        ScreenPoint(width.toFloat(), height.toFloat())
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            // Верхняя панель (без изменений)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    SegmentedButton(selected = tripDirection == TripDirection.TO_WORK, onClick = { tripDirection = TripDirection.TO_WORK }, shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50), label = { Text("На работу") }, icon = { Icon(Icons.Filled.Check, null, tint = if (tripDirection == TripDirection.TO_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface) })
                    SegmentedButton(selected = tripDirection == TripDirection.FROM_WORK, onClick = { tripDirection = TripDirection.FROM_WORK }, shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50), label = { Text("С работы") }, icon = { Icon(Icons.Filled.Check, null, tint = if (tripDirection == TripDirection.FROM_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DateTimePickerButton(dateTime = selectedDateTime, onDateTimeSelected = { selectedDateTime = it }, isDate = true)
                    DateTimePickerButton(dateTime = selectedDateTime, onDateTimeSelected = { selectedDateTime = it }, isDate = false)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (tripDirection == TripDirection.TO_WORK) "Координаты точки старта:" else "Координаты точки назначения:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputLatitudeStr,
                        onValueChange = { inputLatitudeStr = it; isInputLatValid = true },
                        label = { Text("Широта") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true, isError = !isInputLatValid,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused && inputLatitudeStr.isNotBlank()) {
                                val lat = parseCoordinate(inputLatitudeStr, 90.0)
                                val lon = parseCoordinate(inputLongitudeStr, 180.0)
                                isInputLatValid = lat != null || inputLatitudeStr.isBlank()
                                if (isInputLatValid && lat != null && (lon != null || inputLongitudeStr.isBlank())) {
                                    setMapPoint(Point(lat, lon ?: endPointState?.longitude ?: startPointState?.longitude ?: 0.0), true)
                                } else if (!isInputLatValid) {
                                    Toast.makeText(context, "Некорректная широта", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = inputLongitudeStr,
                        onValueChange = { inputLongitudeStr = it; isInputLonValid = true },
                        label = { Text("Долгота") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            localFocusManager.clearFocus()
                            val lat = parseCoordinate(inputLatitudeStr, 90.0)
                            val lon = parseCoordinate(inputLongitudeStr, 180.0)
                            isInputLatValid = lat != null || inputLatitudeStr.isBlank()
                            isInputLonValid = lon != null || inputLongitudeStr.isBlank()
                            if (isInputLatValid && isInputLonValid && lat != null && lon != null) {
                                setMapPoint(Point(lat,lon), true)
                            } else {
                                Toast.makeText(context, "Некорректные координаты", Toast.LENGTH_SHORT).show()
                            }
                        }),
                        singleLine = true, isError = !isInputLonValid,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused && inputLongitudeStr.isNotBlank()) {
                                val lat = parseCoordinate(inputLatitudeStr, 90.0)
                                val lon = parseCoordinate(inputLongitudeStr, 180.0)
                                isInputLonValid = lon != null || inputLongitudeStr.isBlank()
                                if (isInputLonValid && lon != null && (lat != null || inputLatitudeStr.isBlank())) {
                                    setMapPoint(Point(lat ?: startPointState?.latitude ?: endPointState?.latitude ?: 0.0, lon), true)
                                } else if (!isInputLonValid) {
                                    Toast.makeText(context, "Некорректная долгота", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = textFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (drivingRoutePolylineState != null) {
                        showRoleDialog = true
                    } else {
                        Toast.makeText(context, "Задайте точки старта и конца, дождитесь построения маршрута", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(50.dp),
                enabled = drivingRoutePolylineState != null // Активируется, когда маршрут построен
            ) { Text("Подтвердить маршрут") }
        }

        FloatingActionButton(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    yandexLocationManager?.requestSingleUpdate(object : LocationListener {
                        override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                            val userPos = Point(location.position.latitude, location.position.longitude)
                            Log.d("SearchScreenFAB", "User location: $userPos")

                            // 1. Устанавливаем точку (старт или конец) и двигаем карту с зумом 15f
                            setMapPoint(userPos, true, zoomLevel = 15f)

                            // 2. Устанавливаем вторую точку маршрута (компанию) для автоматического построения
                            userCompanyData?.let { company ->
                                val companyLocation = Point(company.latitude, company.longitude)
                                if (tripDirection == TripDirection.TO_WORK) {
                                    // startPointState уже установлен через setMapPoint(userPos, ...)
                                    // Устанавливаем endPointState на компанию, если он еще не установлен или отличается
                                    if (endPointState != companyLocation) {
                                        endPointState = companyLocation
                                        Log.d("SearchScreenFAB", "Set endPointState to company: $companyLocation")
                                    }
                                } else { // TripDirection.FROM_WORK
                                    // endPointState уже установлен через setMapPoint(userPos, ...)
                                    // Устанавливаем startPointState на компанию, если он еще не установлен или отличается
                                    if (startPointState != companyLocation) {
                                        startPointState = companyLocation
                                        Log.d("SearchScreenFAB", "Set startPointState to company: $companyLocation")
                                    }
                                }
                            } ?: run {
                                Toast.makeText(context, "Данные компании не загружены. Укажите вторую точку вручную.", Toast.LENGTH_LONG).show()
                                // Если данных компании нет, маршрут не построится автоматически до/от компании.
                                // Пользователю нужно будет установить вторую точку через поля ввода или другим способом.
                                // `LaunchedEffect(startPointState, endPointState, ...)` не сработает, пока обе точки не будут заданы.
                            }
                        }
                        override fun onLocationStatusUpdated(status: LocationStatus) {
                            if (status == LocationStatus.NOT_AVAILABLE) {
                                Toast.makeText(context, "Геолокация недоступна", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 74.dp).size(50.dp),
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(R.drawable.location),
                contentDescription = if (locationPermissionState.status.isGranted) "Мое местоположение" else "Дать разрешение"
            )
        }
    }

    // Диалог выбора роли (остается без изменений)
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Детали поездки") },
            text = { Column {
                Text("Выберите вашу роль:")
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    RadioButton(selected = isDriverRole, onClick = { isDriverRole = true })
                    Text("Водитель", Modifier.padding(start = 4.dp).align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isDriverRole, onClick = { isDriverRole = false })
                    Text("Пассажир", Modifier.padding(start = 4.dp).align(Alignment.CenterVertically))
                }
                if (isDriverRole) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = seatsInput,
                        onValueChange = { seatsInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Кол-во мест (1-8)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Цена за место (руб)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            } },
            confirmButton = {
                Button(
                    onClick = {
                        val currentUserId = currentUser?.id
                        val currentCompanyId = userCompanyData?.id
                        if (currentUserId != null && currentCompanyId != null && startPointState != null && endPointState != null) {
                            val seats = if (isDriverRole) seatsInput.toIntOrNull() else null
                            val price = if (isDriverRole) priceInput.toDoubleOrNull() else null

                            if (isDriverRole && (seats == null || seats !in 1..8)) {
                                Toast.makeText(context, "Укажите корректное кол-во мест (1-8)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isDriverRole && price == null && priceInput.isNotBlank()){
                                Toast.makeText(context, "Укажите корректную цену или оставьте поле пустым", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            tripViewModel.createTrip(
                                userId = currentUserId,
                                companyId = currentCompanyId,
                                startLat = startPointState!!.latitude,
                                startLon = startPointState!!.longitude,
                                endLat = endPointState!!.latitude,
                                endLon = endPointState!!.longitude,
                                dateTimeMillis = selectedDateTime.timeInMillis,
                                isToWork = (tripDirection == TripDirection.TO_WORK),
                                isDriver = isDriverRole,
                                seatsAvailable = seats,
                                price = price
                            )
                            showRoleDialog = false
                            Toast.makeText(context, "Запрос на поездку создан!", Toast.LENGTH_SHORT).show()

                            // Очистка полей и состояний после создания поездки
                            inputLatitudeStr = ""; inputLongitudeStr = ""
                            // Сброс startPointState/endPointState в зависимости от tripDirection и userCompanyData
                            // Это уже обрабатывается в LaunchedEffect(tripDirection, userCompanyData)
                            // Достаточно переключить tripDirection или дождаться следующего входа на экран
                            // Для немедленного эффекта можно явно сбросить:
                            if (tripDirection == TripDirection.TO_WORK) {
                                startPointState = null // Сбрасываем точку пользователя
                                // endPointState (компания) останется, если userCompanyData не изменился
                            } else {
                                endPointState = null // Сбрасываем точку пользователя
                                // startPointState (компания) останется
                            }
                            drivingRoutePolylineState?.let { mapObjectCollectionState?.remove(it) }
                            drivingRoutePolylineState = null


                        } else {
                            Toast.makeText(context, "Ошибка: не все данные для создания поездки", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Создать") }
            },
            dismissButton = { Button(onClick = { showRoleDialog = false }) { Text("Отмена") } }
        )
    }
}


@Composable
fun RowScope.DateTimePickerButton(
    dateTime: Calendar,
    onDateTimeSelected: (Calendar) -> Unit,
    isDate: Boolean
) {
    val context = LocalContext.current
    val format = if (isDate) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) else SimpleDateFormat("HH:mm", Locale.getDefault())
    val text = format.format(dateTime.time)
    val icon = if (isDate) painterResource(R.drawable.calendar) else painterResource(R.drawable.schedule)

    val year = dateTime.get(Calendar.YEAR)
    val month = dateTime.get(Calendar.MONTH)
    val day = dateTime.get(Calendar.DAY_OF_MONTH)
    val hour = dateTime.get(Calendar.HOUR_OF_DAY)
    val minute = dateTime.get(Calendar.MINUTE)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val newDateTime = (dateTime.clone() as Calendar).apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
            }
            onDateTimeSelected(newDateTime)
        }, year, month, day
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour: Int, selectedMinute: Int ->
            val newDateTime = (dateTime.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            onDateTimeSelected(newDateTime)
        }, hour, minute, true // true для 24-часового формата
    )

    Button(
        onClick = { if (isDate) datePickerDialog.show() else timePickerDialog.show() },
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        shape = RoundedCornerShape(50)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

