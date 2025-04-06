package com.example.ridesynq.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ridesynq.R
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.databinding.MapViewBinding // Убедитесь, что ViewBinding включен и этот файл генерируется
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel
import com.example.ridesynq.viewmodel.TripViewModel // Предполагаем, что этот ViewModel будет создан
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment as LogoAlignment // Используем псевдоним
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import java.text.SimpleDateFormat
import java.util.*

enum class TripDirection { TO_WORK, FROM_WORK }

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    authViewModel: AuthVM,
    companyViewModel: CompanyViewModel // Нужен для координат компании
    // tripViewModel: TripViewModel = viewModel() // Для создания поездки
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val userCompany = remember(currentUser) {
        currentUser?.let { Company(
            id = it.company_id,
            name = "Test Company",
            latitude = 55.751244,
            longitude = 37.618423,
            inn = "",
            kpp = "",
            ogrn = "") }
    }
    var tripDirection by remember { mutableStateOf(TripDirection.TO_WORK) }
    var startPoint by remember { mutableStateOf<Point?>(null) }
    var endPoint by remember { mutableStateOf<Point?>(null) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    val mapView = rememberMapViewWithLifecycle()
    var mapObjectCollection: MapObjectCollection? by remember { mutableStateOf(null) }
    var userLocationLayer: UserLocationLayer? by remember { mutableStateOf(null) }
    var initialCameraPositionSet by remember { mutableStateOf(false) }

    // --- Разрешения ---
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
    // Запрос разрешений при необходимости
    LaunchedEffect(locationPermissionsState) {
        if (!locationPermissionsState.allPermissionsGranted && !locationPermissionsState.shouldShowRationale) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    // ---

    // --- Map Listener ---
    val mapInputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                // Устанавливаем точку в зависимости от направления
                when (tripDirection) {
                    TripDirection.TO_WORK -> {
                        startPoint = point
                        updatePlacemarks(mapObjectCollection, startPoint, endPoint, context)
                    }
                    TripDirection.FROM_WORK -> {
                        endPoint = point
                        updatePlacemarks(mapObjectCollection, startPoint, endPoint, context)
                    }
                }
            }
            override fun onMapLongTap(map: Map, point: Point) {}
        }
    }
    // ---

    // --- Инициализация карты и UserLocationLayer ---
    LaunchedEffect(mapView) {
        mapObjectCollection = mapView.map.mapObjects.addCollection()
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer?.isVisible = locationPermissionsState.allPermissionsGranted
        mapView.map.addInputListener(mapInputListener)

        // Установка логотипа Яндекса
        mapView.map.logo.setAlignment(LogoAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM))
    }

    // --- Обновление видимости слоя пользователя при изменении разрешений ---
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        userLocationLayer?.isVisible = locationPermissionsState.allPermissionsGranted
    }

    // --- Установка начальной позиции камеры ---
    LaunchedEffect(mapView, userCompany, locationPermissionsState.allPermissionsGranted) {
        if (!initialCameraPositionSet && userCompany != null) {
            mapView.map.move(
                CameraPosition(Point(userCompany.latitude, userCompany.longitude), 14.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
            initialCameraPositionSet = true
        } else if (!initialCameraPositionSet && locationPermissionsState.allPermissionsGranted) {
            // Попытка центрироваться на пользователе, если нет данных о компании
            // Нужна обработка UserLocationObjectListener для получения первой позиции
        }
    }

    // --- Обновление точек при смене направления ---
    LaunchedEffect(tripDirection, userCompany) {
        if (userCompany != null) {
            when (tripDirection) {
                TripDirection.TO_WORK -> {
                    startPoint = null // Сбрасываем старт, пользователь выберет
                    endPoint = Point(userCompany.latitude, userCompany.longitude)
                }
                TripDirection.FROM_WORK -> {
                    startPoint = Point(userCompany.latitude, userCompany.longitude)
                    endPoint = null // Сбрасываем конец, пользователь выберет
                }
            }
            updatePlacemarks(mapObjectCollection, startPoint, endPoint, context)
            // Перемещаем камеру к точке компании, если она изменилась (старт или конец)
            val targetPoint = if(tripDirection == TripDirection.TO_WORK) endPoint else startPoint
            targetPoint?.let {
                mapView.map.move(
                    CameraPosition(it, 14.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 0.5f),
                    null
                )
            }

        }
    }

    // --- UI ---
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidViewBinding(
            factory = MapViewBinding::inflate,
            modifier = Modifier.fillMaxSize(),
            update = { /*MapView обновляется через LaunchedEffect и listener'ы*/ }
        )

        // Элементы управления поверх карты
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween // Распределяем элементы
        ) {
            // Верхние элементы (Выбор направления, Дата/Время)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Выбор направления
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    SegmentedButton(
                        selected = tripDirection == TripDirection.TO_WORK,
                        onClick = { tripDirection = TripDirection.TO_WORK },
                        shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50), // Скругляем левую
                        label = { Text("На работу") },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = if(tripDirection == TripDirection.TO_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface) } // Иконка для выделения
                    )
                    SegmentedButton(
                        selected = tripDirection == TripDirection.FROM_WORK,
                        onClick = { tripDirection = TripDirection.FROM_WORK },
                        shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50), // Скругляем правую
                        label = { Text("С работы") },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = if(tripDirection == TripDirection.FROM_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)}
                    )
                }

                // Выбор даты и времени
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateTimePickerButton(
                        dateTime = selectedDateTime,
                        onDateTimeSelected = { selectedDateTime = it },
                        isDate = true
                    )
                    DateTimePickerButton(
                        dateTime = selectedDateTime,
                        onDateTimeSelected = { selectedDateTime = it },
                        isDate = false
                    )
                }
                // Информационная подсказка
                Card(modifier = Modifier.padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = if (tripDirection == TripDirection.TO_WORK) "Укажите точку старта на карте" else "Укажите точку назначения на карте",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }





            // Нижняя кнопка (Запросить поездку)
            Button(
                onClick = {
                    // TODO: Логика создания поездки
                    if (startPoint != null && endPoint != null && currentUser != null) {
                        // tripViewModel.createTripRequest(...)
                        println("Requesting trip: Start=$startPoint, End=$endPoint, Time=${selectedDateTime.timeInMillis}, User=${currentUser!!.id}, Direction=$tripDirection")
                        // Показать Toast, навигация и т.д.
                    } else {
                        println("Error: Missing data for trip request.")
                        // Показать сообщение об ошибке
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp) // Отступы для кнопки
                    .height(50.dp),
                enabled = startPoint != null && endPoint != null && currentUser != null // Активна, если все данные есть
            ) {
                Text("Запросить поездку")
            }
        }
        // Кнопка "Мое местоположение" (показываем, если есть разрешения)
        if (locationPermissionsState.allPermissionsGranted) {
            FloatingActionButton(
                onClick = {
                    userLocationLayer?.cameraPosition()?.let { currentPosition ->
                        if (tripDirection == TripDirection.TO_WORK) {
                            startPoint = currentPosition.target
                            mapView.map.move(
                                CameraPosition(startPoint!!, 15.0f, 0.0f, 0.0f),
                                Animation(Animation.Type.SMOOTH, 0.5f),
                                null
                            )
                        } else {
                            // Для поездки "С работы" эта кнопка может быть менее полезна,
                            // т.к. старт фиксирован. Можно использовать для центрирования карты.
                            mapView.map.move(
                                CameraPosition(currentPosition.target, 15.0f, 0.0f, 0.0f),
                                Animation(Animation.Type.SMOOTH, 0.5f),
                                null
                            )
                        }
                        updatePlacemarks(mapObjectCollection, startPoint, endPoint, context)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, bottom = 80.dp) // Отступ от нижней кнопки
            ) {
                Icon(painterResource(R.drawable.location), "My Location")
            }
        }
    }
}

// --- Вспомогательные функции и компоненты ---

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                Lifecycle.Event.ON_PAUSE -> mapView.onStop() // MapKit рекомендует onStop в ON_PAUSE
                Lifecycle.Event.ON_RESUME -> mapView.onStart() // и onStart в ON_RESUME
                else -> {} // Остальные события не требуют действий для MapKit/MapView
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            // Очистка ресурсов карты при удалении Composable (не обязательно для lifecycle)
            // mapView.map.clear()
        }
    }
    return mapView
}

// Обновление меток на карте
fun updatePlacemarks(collection: MapObjectCollection?, start: Point?, end: Point?, context: Context) {
    collection?.clear() // Очищаем старые метки
    val startIcon = ImageProvider.fromResource(context, R.drawable.placemark_start) // Нужны иконки
    val endIcon = ImageProvider.fromResource(context, R.drawable.placemark_end) // Нужны иконки

    start?.let {
        collection?.addPlacemark(it, startIcon)
    }
    end?.let {
        collection?.addPlacemark(it, endIcon)
    }
}


// Компонент для выбора даты/времени
@Composable
fun RowScope.DateTimePickerButton(
    dateTime: Calendar,
    onDateTimeSelected: (Calendar) -> Unit,
    isDate: Boolean // true для даты, false для времени
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
        shape = RoundedCornerShape(50) // Полностью скругленные
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

