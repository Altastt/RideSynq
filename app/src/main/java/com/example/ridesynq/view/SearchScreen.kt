package com.example.ridesynq.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesynq.R
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.databinding.MapViewBinding
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel
import com.example.ridesynq.viewmodel.TripVMFactory
import com.example.ridesynq.viewmodel.TripViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment as LogoAlignment
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
    companyViewModel: CompanyViewModel,
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()


    val appDatabase = AppDatabase.getDatabase(context)
    val tripViewModel: TripViewModel = viewModel(
        factory = TripVMFactory(appDatabase.tripDao(), appDatabase.userTripDao()) // Пример фабрики
    )


    val allCompanies by companyViewModel.allCompanies.collectAsState(initial = emptyList())
    val userCompanyData = remember(currentUser, allCompanies) { // Передаем allCompanies как ключ
        currentUser?.let { user ->
            val companyEntity = allCompanies.find { it.id == user.company_id }
            Company(
                id = user.company_id,
                name = companyEntity?.name ?: "Компания пользователя",
                latitude = companyEntity?.latitude ?: 55.751244,
                longitude = companyEntity?.longitude ?: 37.618423,
                inn = companyEntity?.inn ?: "",
                kpp = companyEntity?.kpp ?: "",
                ogrn = companyEntity?.ogrn ?: ""
            )
        }
    }

    val targetCoordsFromVM by companyViewModel.mapTargetCoordinates.collectAsState()

    var tripDirection by remember { mutableStateOf(TripDirection.TO_WORK) }
    var startPoint by remember { mutableStateOf<Point?>(null) }
    var endPoint by remember { mutableStateOf<Point?>(null) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }

    val localContext = LocalContext.current
    val localLifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(localContext, localLifecycleOwner) { // Передаем context и lifecycleOwner как ключи
        MapView(localContext).apply {
            val lifecycle = localLifecycleOwner.lifecycle
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> { MapKitFactory.getInstance().onStart(); this.onStart() }
                    Lifecycle.Event.ON_STOP -> { this.onStop(); MapKitFactory.getInstance().onStop() }
                    Lifecycle.Event.ON_PAUSE -> this.onStop()
                    Lifecycle.Event.ON_RESUME -> this.onStart()
                    else -> {}
                }
            }
            lifecycle.addObserver(observer)
            // onDispose не нужен в remember, если мы используем DisposableEffect для очистки
        }
    }
    // Используем DisposableEffect для очистки ресурсов MapView при выходе из композиции
    DisposableEffect(mapView) {
        onDispose {
            // Явное удаление слушателя из lifecycle здесь не нужно,
            // т.к. он привязан к lifecycle mapView, которая будет уничтожена.
            // Можно добавить mapView.onStop() и MapKitFactory.getInstance().onStop() здесь,
            // если это не обрабатывается корректно в LifecycleEventObserver при быстром выходе.
            // Однако, Yandex MapKit обычно хорошо управляет своим жизненным циклом через onStart/onStop.
        }
    }
    var mapObjectCollection: MapObjectCollection? by remember { mutableStateOf(null) }
    var userLocationLayer: UserLocationLayer? by remember { mutableStateOf(null) }

    // Точка, которая пришла из CompanyScreen (или данные компании пользователя по умолчанию)
    // Используется для постоянного отображения метки компании и центрирования
    var companyDisplayPoint: Point? by remember { mutableStateOf(null) }
    var initialCenteringDone by remember { mutableStateOf(false) }


    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    LaunchedEffect(locationPermissionsState) {
        if (!locationPermissionsState.allPermissionsGranted && !locationPermissionsState.shouldShowRationale) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    val mapInputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                when (tripDirection) {
                    TripDirection.TO_WORK -> startPoint = point
                    TripDirection.FROM_WORK -> endPoint = point
                }
                updatePlacemarks(mapObjectCollection, startPoint, endPoint, context, companyDisplayPoint)
            }
            override fun onMapLongTap(map: Map, point: Point) {}
        }
    }

    LaunchedEffect(mapView) {
        mapObjectCollection = mapView.map.mapObjects.addCollection()
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer?.isVisible = locationPermissionsState.allPermissionsGranted
        mapView.map.addInputListener(mapInputListener)
        mapView.map.logo.setAlignment(LogoAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM))
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        userLocationLayer?.isVisible = locationPermissionsState.allPermissionsGranted
    }

    // Центрирование карты при первом запуске или при получении targetCoords
    LaunchedEffect(targetCoordsFromVM, userCompanyData, mapView, initialCenteringDone) {
        if (!initialCenteringDone) {
            val pointToCenter: Point?
            if (targetCoordsFromVM != null) {
                Log.d("SearchScreen", "Centering on targetCoordsFromVM: $targetCoordsFromVM")
                pointToCenter = Point(targetCoordsFromVM!!.first, targetCoordsFromVM!!.second)
                companyDisplayPoint = pointToCenter // Эта метка будет постоянной
                companyViewModel.consumeMapTarget() // Сбрасываем после использования
            } else if (userCompanyData != null) {
                Log.d("SearchScreen", "Centering on userCompanyData: ${userCompanyData!!.latitude}, ${userCompanyData!!.longitude}")
                pointToCenter = Point(userCompanyData!!.latitude, userCompanyData!!.longitude)
                companyDisplayPoint = pointToCenter
            } else {
                pointToCenter = null
            }

            pointToCenter?.let {
                mapView.map.move(
                    CameraPosition(it, 15.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 0.8f),
                    null
                )
                updatePlacemarks(mapObjectCollection, startPoint, endPoint, context, companyDisplayPoint)
                initialCenteringDone = true // Центрирование выполнено
            }
        }
    }

    // Обновление точек старта/конца при смене направления
    LaunchedEffect(tripDirection, userCompanyData) {
        userCompanyData?.let { company ->
            when (tripDirection) {
                TripDirection.TO_WORK -> {
                    startPoint = null
                    endPoint = Point(company.latitude, company.longitude)
                }
                TripDirection.FROM_WORK -> {
                    startPoint = Point(company.latitude, company.longitude)
                    endPoint = null
                }
            }
            updatePlacemarks(mapObjectCollection, startPoint, endPoint, context, companyDisplayPoint)
        }
    }

    // Состояния для диалога "Я водитель/пассажир"
    var showRoleDialog by remember { mutableStateOf(false) }
    var isDriverRole by remember { mutableStateOf(true) } // По умолчанию водитель
    var seatsInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidViewBinding(
            factory = MapViewBinding::inflate,
            modifier = Modifier.fillMaxSize(),
            update = { /* No direct updates needed here */ }
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
        ) {
            // Верхние элементы (Выбор направления, Дата/Время, Подсказка)
            Column(modifier = Modifier.fillMaxWidth()) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    SegmentedButton(
                        selected = tripDirection == TripDirection.TO_WORK,
                        onClick = { tripDirection = TripDirection.TO_WORK },
                        shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
                        label = { Text("На работу") },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = if(tripDirection == TripDirection.TO_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface) }
                    )
                    SegmentedButton(
                        selected = tripDirection == TripDirection.FROM_WORK,
                        onClick = { tripDirection = TripDirection.FROM_WORK },
                        shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50),
                        label = { Text("С работы") },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = if(tripDirection == TripDirection.FROM_WORK) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)}
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateTimePickerButton(dateTime = selectedDateTime, onDateTimeSelected = { selectedDateTime = it }, isDate = true)
                    DateTimePickerButton(dateTime = selectedDateTime, onDateTimeSelected = { selectedDateTime = it }, isDate = false)
                }
                Card(
                    modifier = Modifier.padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (tripDirection == TripDirection.TO_WORK) "Тапните на карту, чтобы указать точку старта" else "Тапните на карту, чтобы указать точку назначения",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (startPoint != null && endPoint != null && currentUser != null && userCompanyData != null) {
                        showRoleDialog = true // Открываем диалог для выбора роли и деталей
                    } else {
                        Toast.makeText(context, "Укажите все точки маршрута и убедитесь, что данные компании загружены", Toast.LENGTH_LONG).show()
                        Log.w("SearchScreen","Error: Missing data. Start: $startPoint, End: $endPoint, User: $currentUser, Company: $userCompanyData")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(50.dp),
                enabled = startPoint != null && endPoint != null && currentUser != null && userCompanyData != null
            ) {
                Text("Подтвердить маршрут") // Изменил текст кнопки
            }
            Spacer(modifier = Modifier.height(70.dp)) // Отступ для BottomBar
        }

        if (locationPermissionsState.allPermissionsGranted) {
            FloatingActionButton(
                onClick = {
                    userLocationLayer?.cameraPosition()?.let { currentPosition ->
                        if (tripDirection == TripDirection.TO_WORK) {
                            startPoint = currentPosition.target
                            mapView.map.move(
                                CameraPosition(startPoint!!, 15.0f, 0.0f, 0.0f),
                                Animation(Animation.Type.SMOOTH, 0.5f), null
                            )
                        } else { // FROM_WORK
                            // Центрируем карту на текущем местоположении, но не меняем startPoint
                            mapView.map.move(
                                CameraPosition(currentPosition.target, 15.0f, 0.0f, 0.0f),
                                Animation(Animation.Type.SMOOTH, 0.5f), null
                            )
                        }
                        updatePlacemarks(mapObjectCollection, startPoint, endPoint, context, companyDisplayPoint)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 86.dp) // bottom: 70 (отступ кнопки) + 16 (отступ FAB)
                    .size(25.dp), // Уменьшенный размер
                shape = CircleShape,
            ) {
                Icon(painterResource(R.drawable.location), "Мое местоположение")
            }
        }
    }


    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Детали поездки") },
            text = {
                Column {
                    Text("Выберите вашу роль:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row { /* ... RadioButtons ... */
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentUserId = currentUser?.id
                        val currentCompanyId = userCompanyData?.id
                        if (currentUserId != null && currentCompanyId != null && startPoint != null && endPoint != null) {
                            val seats = if (isDriverRole) seatsInput.toIntOrNull() else null
                            val price = if (isDriverRole) priceInput.toDoubleOrNull() else null

                            if (isDriverRole && (seats == null || seats !in 1..8)) {
                                Toast.makeText(context, "Укажите корректное кол-во мест (1-8)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isDriverRole && price == null && priceInput.isNotBlank()){ // если цена введена но не парсится
                                Toast.makeText(context, "Укажите корректную цену", Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            tripViewModel.createTrip(
                                userId = currentUserId,
                                companyId = currentCompanyId,
                                startLat = startPoint!!.latitude,
                                startLon = startPoint!!.longitude,
                                endLat = endPoint!!.latitude,
                                endLon = endPoint!!.longitude,
                                dateTimeMillis = selectedDateTime.timeInMillis,
                                isToWork = (tripDirection == TripDirection.TO_WORK), // Передаем isToWork
                                isDriver = isDriverRole,
                                seatsAvailable = seats,
                                price = price
                            )
                            showRoleDialog = false
                            Toast.makeText(context, "Запрос на поездку создан!", Toast.LENGTH_SHORT).show()
                            // Опционально: сбросить точки или перейти на TripScreen
                            // startPoint = null
                            // endPoint = null
                            // updatePlacemarks(...)
                        } else {
                            Toast.makeText(context, "Ошибка: не все данные для создания поездки", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Создать") }
            },
            dismissButton = {
                Button(onClick = { showRoleDialog = false }) { Text("Отмена") }
            }
        )
    }
}


fun updatePlacemarks(
    collection: MapObjectCollection?,
    start: Point?,
    end: Point?,
    context: Context,
    companyPointToDisplay: Point?
) {
    collection?.clear()

    val startIcon = ImageProvider.fromResource(context, R.drawable.placemark_start)
    val endIcon = ImageProvider.fromResource(context, R.drawable.placemark_end)
    val companyIcon = ImageProvider.fromResource(context, R.drawable.location)

    companyPointToDisplay?.let {
        collection?.addPlacemark(it, companyIcon)
    }

    start?.let {
        if (it != companyPointToDisplay) {
            collection?.addPlacemark(it, startIcon)
        }
    }
    end?.let {
        if (it != companyPointToDisplay) {
            collection?.addPlacemark(it, endIcon)
        }
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
        }, hour, minute, true
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