package com.example.ridesynq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserTripDao
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip
import com.example.ridesynq.data.relations.TripWithUsers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TripViewModel(
    private val tripDao: TripDao,
    private val userTripDao: UserTripDao,
    private val authVM: AuthVM // Добавляем AuthVM для доступа к currentUser
) : ViewModel() {

    sealed class CreateTripUiState {
        object Idle : CreateTripUiState()
        object Loading : CreateTripUiState()
        object Success : CreateTripUiState()
        data class Error(val message: String) : CreateTripUiState()
    }
    private val _createTripState = MutableStateFlow<CreateTripUiState>(CreateTripUiState.Idle)
    val createTripState = _createTripState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        object Success : ActionState()
        data class Error(val message: String) : ActionState()
    }

    private val currentUser: StateFlow<User?> = authVM.currentUser

    // Загружаем все активные и будущие поездки, затем фильтруем
    private val allRelevantTripsWithUsers: Flow<List<TripWithUsers>> =
        tripDao.getAllActiveAndFutureTripsWithUsers()
            .map { tripsWithUsers ->
                val currentTimeMillis = Calendar.getInstance().timeInMillis
                // Сначала проверим и обновим статусы активных поездок на finished
                tripsWithUsers.forEach { tripData ->
                    if (tripData.trip.status == "active" && tripData.trip.activatedAt != null) {
                        val twoHoursInMillis = 2 * 60 * 60 * 1000
                        if (currentTimeMillis > (tripData.trip.activatedAt!! + twoHoursInMillis)) {
                            viewModelScope.launch { // Обновляем в фоне
                                tripDao.updateTripStatus(tripData.trip.id, "finished")
                                Log.d("TripViewModel", "Trip ${tripData.trip.id} automatically set to finished.")
                            }
                        }
                    }
                }
                // Возвращаем только те, что еще не "finished" и актуальны по времени
                tripsWithUsers.filter {
                    it.trip.status != "finished" && it.trip.datetime >= currentTimeMillis
                }
            }
            .distinctUntilChanged() // Чтобы избежать лишних обновлений, если список не изменился

    // Поездки, инициированные водителями
    val driverInitiatedTrips: StateFlow<List<TripWithUsers>> =
        combine(allRelevantTripsWithUsers, currentUser) { trips, user ->
            trips.filter { it.trip.driverId != null }
                .map { tripWithUsers -> // tripWithUsers - это объект TripWithUsers
                    val isCurrentUserDriver = tripWithUsers.trip.driverId == user?.id
                    val isCurrentUserPassenger = tripWithUsers.users.any { u -> u.id == user?.id && u.id != tripWithUsers.trip.driverId }

                    // Устанавливаем @Ignore поля напрямую
                    tripWithUsers.canCurrentUserJoin = !isCurrentUserDriver && !isCurrentUserPassenger && tripWithUsers.trip.status == "pending" && (tripWithUsers.trip.seatsAvailable ?: 0) > 0
                    tripWithUsers.isCurrentUserParticipant = isCurrentUserDriver || isCurrentUserPassenger
                    // canCurrentUserTakeRide останется false (значение по умолчанию) для водительских поездок

                    tripWithUsers // Возвращаем модифицированный объект
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Поездки, инициированные пассажирами (запросы на поездку)
    val passengerInitiatedTrips: StateFlow<List<TripWithUsers>> =
        combine(allRelevantTripsWithUsers, currentUser) { trips, user ->
            trips.filter { it.trip.driverId == null } // Запросы от пассажиров
                .map { tripWithUsers ->
                    val isCurrentUserInitiator = tripWithUsers.users.any { u -> u.id == user?.id }

                    // Устанавливаем @Ignore поля напрямую
                    tripWithUsers.canCurrentUserTakeRide = user?.transport_name != null && !isCurrentUserInitiator && tripWithUsers.trip.status == "pending"
                    tripWithUsers.isCurrentUserParticipant = isCurrentUserInitiator
                    // canCurrentUserJoin останется false (значение по умолчанию) для пассажирских запросов

                    tripWithUsers // Возвращаем модифицированный объект
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTrip(
        userId: Int,
        companyId: Int,
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        dateTimeMillis: Long,
        isToWork: Boolean,
        isDriver: Boolean,
        seatsAvailable: Int?,
        price: Double?
    ) {
        _createTripState.value = CreateTripUiState.Loading
        viewModelScope.launch {
            try {
                val newTrip = Trip(
                    companyId = companyId,
                    datetime = dateTimeMillis,
                    startLatitude = startLat,
                    startLongitude = startLon,
                    endLatitude = endLat,
                    endLongitude = endLon,
                    isToWork = isToWork,
                    driverId = if (isDriver) userId else null,
                    seatsAvailable = if (isDriver) seatsAvailable else null,
                    pricePerSeat = if (isDriver) price else null,
                    status = "pending",
                    activatedAt = null // Изначально null
                )
                val tripId = tripDao.insertTripAndGetId(newTrip)
                if (tripId > 0) {
                    val userTrip = UserTrip(
                        userId = userId,
                        tripId = tripId.toInt(),
                        role = if (isDriver) "driver" else "passenger"
                    )
                    userTripDao.insert(userTrip)
                    _createTripState.value = CreateTripUiState.Success
                } else {
                    _createTripState.value = CreateTripUiState.Error("Ошибка сохранения поездки")
                }
            } catch (e: Exception) {
                _createTripState.value = CreateTripUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun resetCreateTripState() {
        _createTripState.value = CreateTripUiState.Idle
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }


    // Пользователь (пассажир) занимает место в поездке водителя
    fun joinTripAsPassenger(tripId: Int) {
        val passenger = currentUser.value ?: return // Нужен текущий пользователь
        _actionState.value = ActionState.Loading
        viewModelScope.launch {
            try {
                val trip = tripDao.getTripById(tripId)
                if (trip != null && trip.status == "pending" && (trip.seatsAvailable ?: 0) > 0) {
                    val newSeatsAvailable = (trip.seatsAvailable ?: 1) - 1
                    tripDao.updateTripSeatsAndStatus(
                        tripId = tripId,
                        seatsAvailable = newSeatsAvailable,
                        status = "active", // Всегда "active" при успешном присоединении
                        activatedAt = Calendar.getInstance().timeInMillis // Всегда устанавливаем время активации
                    )

                    // 2. Добавляем пользователя в UserTrip как пассажира
                    val userTrip = UserTrip(
                        userId = passenger.id,
                        tripId = tripId,
                        role = "passenger"
                    )
                    userTripDao.insert(userTrip)
                    Log.d("TripViewModel", "User ${passenger.id} joined trip $tripId as passenger. Seats left: $newSeatsAvailable")
                    _actionState.value = ActionState.Success
                } else {
                    Log.e("TripViewModel", "Cannot join trip $tripId: trip not found, not pending, or no seats.")
                    _actionState.value = ActionState.Error("Не удалось присоединиться к поездке (нет мест или поездка неактуальна)")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error joining trip: ${e.message}", e)
                _actionState.value = ActionState.Error(e.message ?: "Ошибка присоединения к поездке")
            }
        }
    }

    // Водитель берет запрос пассажира
    fun takePassengerRequest(tripId: Int) {
        val driver = currentUser.value ?: return // Нужен текущий пользователь (водитель)
        if (driver.transport_name == null) {
            _actionState.value = ActionState.Error("У вас не указана информация об автомобиле")
            return
        }
        _actionState.value = ActionState.Loading
        viewModelScope.launch {
            try {
                val trip = tripDao.getTripById(tripId)
                if (trip != null && trip.status == "pending" && trip.driverId == null) {
                    // 1. Обновляем поездку: назначаем водителя, статус, activatedAt
                    tripDao.assignDriverToTrip(
                        tripId = tripId,
                        driverId = driver.id,
                        status = "active",
                        activatedAt = Calendar.getInstance().timeInMillis
                    )

                    // 2. Добавляем водителя в UserTrip (если его там еще нет, хотя создатель запроса уже должен быть)
                    // На всякий случай, проверим и добавим, если нужно
                    val existingDriverEntry = userTripDao.getUserTrip(driver.id, tripId).firstOrNull()
                    if (existingDriverEntry == null) {
                        val userTrip = UserTrip(
                            userId = driver.id,
                            tripId = tripId,
                            role = "driver"
                        )
                        userTripDao.insert(userTrip)
                    } else if (existingDriverEntry.role != "driver") {
                        // Если он был как пассажир (маловероятно для этой логики, но все же)
                        userTripDao.updateUserRoleInTrip(driver.id, tripId, "driver")
                    }

                    Log.d("TripViewModel", "Driver ${driver.id} took passenger request $tripId.")
                    _actionState.value = ActionState.Success
                } else {
                    Log.e("TripViewModel", "Cannot take passenger request $tripId: trip not found, not pending, or already has a driver.")
                    _actionState.value = ActionState.Error("Не удалось взять заявку (заявка неактуальна или уже принята)")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error taking passenger request: ${e.message}", e)
                _actionState.value = ActionState.Error(e.message ?: "Ошибка принятия заявки")
            }
        }
    }
}