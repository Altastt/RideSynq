package com.example.ridesynq.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesynq.R
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.relations.TripWithUsers
import com.example.ridesynq.viewmodel.TripVMFactory
import com.example.ridesynq.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons // Для иконок (если нужны)
import androidx.compose.material.icons.filled.Person // Пример иконки
import androidx.compose.ui.res.painterResource
import com.example.ridesynq.models.NavigationItems // Для навигации на SearchScreen
import androidx.navigation.NavController // Для навигации

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    navController: NavController, // Добавляем NavController
    // AuthVM можно получить через activityViewModels(), если нужен ID текущего пользователя для "Мои поездки"
) {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val tripViewModel: TripViewModel = viewModel(
        factory = TripVMFactory(appDatabase.tripDao(), appDatabase.userTripDao())
    )

    val driverTrips by tripViewModel.driverInitiatedTrips.collectAsState()
    val passengerTrips by tripViewModel.passengerInitiatedTrips.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Поездки водителей", "Запросы пассажиров")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Активные поездки") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> TripList(tripsWithUsers = driverTrips, "водителей")
                1 -> TripList(tripsWithUsers = passengerTrips, "пассажиров")
            }

            // Кнопка создания поездки может быть здесь или как FAB
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Button(
                    onClick = {
                        // Переход на SearchScreen для создания новой поездки
                        navController.navigate(NavigationItems.Search.route) {
                            // Опционально: настройки popUpTo
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp) // Отступ от низа
                        .width(200.dp)
                        .height(50.dp)
                ) {
                    Text(stringResource(R.string.create_trip))
                }
            }
        }
    }
}

@Composable
fun TripList(tripsWithUsers: List<TripWithUsers>, tripType: String) {
    if (tripsWithUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет активных поездок $tripType", style = MaterialTheme.typography.headlineSmall)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tripsWithUsers) { tripData ->
                TripCard(tripData = tripData)
            }
        }
    }
}

@Composable
fun TripCard(tripData: TripWithUsers) {
    val trip = tripData.trip
    val usersInTrip = tripData.users // Список User объектов, участвующих в поездке

    // Форматирование даты и времени
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("ru")) }
    val tripDateTime = remember(trip.datetime) { dateFormat.format(Date(trip.datetime)) }

    // Определение, кто водитель (если есть)
    val driver = usersInTrip.find { it.id == trip.driverId } // Ищем водителя по driverId в Trip

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (trip.isToWork) "На работу" else "С работы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Время: $tripDateTime", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            // Отображение точек маршрута (пока просто координаты)
            // TODO: Геокодирование для получения названий мест
            Text("Старт: (${String.format("%.3f", trip.startLatitude)}, ${String.format("%.3f", trip.startLongitude)})", style = MaterialTheme.typography.bodySmall)
            Text("Финиш: (${String.format("%.3f", trip.endLatitude)}, ${String.format("%.3f", trip.endLongitude)})", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            if (driver != null) { // Это поездка водителя
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.add_car), contentDescription = "Водитель", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Водитель: ${driver.firstname ?: ""} ${driver.lastname ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text("Мест свободно: ${trip.seatsAvailable ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                Text("Цена: ${trip.pricePerSeat?.toString() ?: "Бесплатно"} руб.", style = MaterialTheme.typography.bodySmall)

            } else { // Это запрос от пассажира
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = "Пассажир", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Запрос от пассажира(ов):", style = MaterialTheme.typography.bodyMedium)
                }
                // Можно вывести список пассажиров, если их несколько
                usersInTrip.forEach { passenger ->
                    Text("- ${passenger.firstname ?: ""} ${passenger.lastname ?: ""}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Статус: ${trip.status.replaceFirstChar { it.titlecase(Locale.getDefault()) }}", style = MaterialTheme.typography.bodySmall) // Pending, Confirmed и т.д.

            // TODO: Добавить кнопки действий (Присоединиться, Отменить, Посмотреть детали и т.д.)
            // Например:
            // Button(onClick = { /* TODO */ }) { Text("Присоединиться") }
        }
    }
}