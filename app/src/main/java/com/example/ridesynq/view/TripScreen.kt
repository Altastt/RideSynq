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
import androidx.lifecycle.viewmodel.compose.viewModel // Для viewModel()
import com.example.ridesynq.R
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.relations.TripWithUsers
import com.example.ridesynq.viewmodel.TripVMFactory
import com.example.ridesynq.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.painterResource
import com.example.ridesynq.models.NavigationItems
import androidx.navigation.NavController
import com.example.ridesynq.viewmodel.AuthVM
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner // Для AuthVM
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.lifecycle.ViewModelStoreOwner // Для AuthVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    navController: NavController,
    authViewModel: AuthVM // Получаем AuthVM из графа навигации или activity
) {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)

    // Передаем authViewModel в фабрику
    val tripViewModel: TripViewModel = viewModel(
        factory = TripVMFactory(appDatabase.tripDao(), appDatabase.userTripDao(), authViewModel)
    )

    val driverTrips by tripViewModel.driverInitiatedTrips.collectAsState()
    val passengerTrips by tripViewModel.passengerInitiatedTrips.collectAsState()
    val actionState by tripViewModel.actionState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Поездки водителей", "Запросы пассажиров")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is TripViewModel.ActionState.Success -> {
                snackbarHostState.showSnackbar("Действие выполнено успешно!")
                tripViewModel.resetActionState()
            }
            is TripViewModel.ActionState.Error -> {
                snackbarHostState.showSnackbar("Ошибка: ${state.message}")
                tripViewModel.resetActionState()
            }
            TripViewModel.ActionState.Idle -> {}
            TripViewModel.ActionState.Loading -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Активные поездки") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(NavigationItems.Search.route)
            }) {
                Icon(painterResource(R.drawable.add),
                    contentDescription = stringResource(R.string.create_trip),
                    modifier = Modifier.size(50.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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

            if (actionState is TripViewModel.ActionState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> TripList(
                        tripsWithUsers = driverTrips,
                        tripType = "водителей",
                        onJoinTrip = { tripId -> tripViewModel.joinTripAsPassenger(tripId) },
                        onTakeRide = null // Для вкладки водителей это не нужно
                    )
                    1 -> TripList(
                        tripsWithUsers = passengerTrips,
                        tripType = "пассажиров",
                        onJoinTrip = null, // Для вкладки пассажиров это не нужно
                        onTakeRide = { tripId -> tripViewModel.takePassengerRequest(tripId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TripList(
    tripsWithUsers: List<TripWithUsers>,
    tripType: String,
    onJoinTrip: ((Int) -> Unit)?, // Колбэк для "Занять место"
    onTakeRide: ((Int) -> Unit)?  // Колбэк для "Подвезти"
) {
    if (tripsWithUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Нет активных поездок $tripType", style = MaterialTheme.typography.headlineSmall)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tripsWithUsers) { tripData ->
                TripCard(
                    tripData = tripData,
                    onJoinTrip = onJoinTrip,
                    onTakeRide = onTakeRide
                )
            }
        }
    }
}

@Composable
fun TripCard(
    tripData: TripWithUsers,
    onJoinTrip: ((Int) -> Unit)?,
    onTakeRide: ((Int) -> Unit)?
) {
    val trip = tripData.trip
    val usersInTrip = tripData.users

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("ru")) }
    val tripDateTime = remember(trip.datetime) { dateFormat.format(Date(trip.datetime)) }

    val driver = usersInTrip.find { it.id == trip.driverId }
    val initiator: com.example.ridesynq.data.entities.User? = if (trip.driverId != null) {
        usersInTrip.find { it.id == trip.driverId }
    } else {
        usersInTrip.firstOrNull()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (trip.isToWork) "На работу" else "С работы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Статус: ${trip.status.replaceFirstChar { it.uppercase(Locale.getDefault()) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when(trip.status) {
                        "pending" -> MaterialTheme.colorScheme.secondary
                        "active" -> MaterialTheme.colorScheme.primary
                        "finished" -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Время: $tripDateTime", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Старт: (${String.format("%.3f", trip.startLatitude)}, ${String.format("%.3f", trip.startLongitude)})", style = MaterialTheme.typography.bodySmall)
            Text("Финиш: (${String.format("%.3f", trip.endLatitude)}, ${String.format("%.3f", trip.endLongitude)})", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            // Отображение информации об инициаторе
            if (initiator != null) {
                Text(
                    text = "Инициатор: ${initiator.firstname ?: ""} ${initiator.lastname ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold // Немного выделим
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = initiator.login, // Предполагаем, что login это email
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!initiator.phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Телефон",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = initiator.phone!!,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp)) // Добавим отступ перед деталями поездки/запроса
            }


            // Логика отображения деталей поездки водителя или запроса пассажира
            if (trip.driverId != null) { // Это поездка, инициированная водителем
                // initiator здесь будет водителем
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.add_car), contentDescription = "Водитель", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    // Имя водителя уже выведено как "Инициатор"
                    Text(
                        text = "Детали поездки водителя:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text("Мест свободно: ${trip.seatsAvailable ?: "0"}", style = MaterialTheme.typography.bodySmall)
                Text("Цена: ${trip.pricePerSeat?.toString() ?: "Бесплатно"} руб.", style = MaterialTheme.typography.bodySmall)

                if (tripData.canCurrentUserJoin && onJoinTrip != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onJoinTrip(trip.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = trip.status == "pending"
                    ) {
                        Text("Занять место")
                    }
                }

            } else { // Это запрос, инициированный пассажиром
                // initiator здесь будет пассажиром-создателем запроса
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = "Пассажир", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Детали запроса пассажира:", style = MaterialTheme.typography.bodyMedium)
                }
                // Можно дополнительно вывести список всех пассажиров, если в запросе их несколько
                // и это отличается от initiator (хотя для простого запроса initiator - единственный участник изначально)
                if (usersInTrip.size > 1 && initiator != null) {
                    Text("Другие участники запроса:", style = MaterialTheme.typography.labelSmall)
                    usersInTrip.filter { it.id != initiator.id }.forEach { passenger ->
                        Text("- ${passenger.firstname ?: ""} ${passenger.lastname ?: ""}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                    }
                } else if (usersInTrip.isEmpty() && initiator == null) {
                    // Этот случай не должен происходить, если логика TripWithUsers и UserTrip корректна
                    Text("Нет информации об участниках запроса.", style = MaterialTheme.typography.bodySmall)
                }


                if (tripData.canCurrentUserTakeRide && onTakeRide != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onTakeRide(trip.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = trip.status == "pending"
                    ) {
                        Text("Подвезти")
                    }
                }
            }

            if (tripData.isCurrentUserParticipant && trip.status != "finished") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Вы участвуете в этой поездке", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}