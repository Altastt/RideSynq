package com.example.ridesynq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserTripDao
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.UserTrip
import com.example.ridesynq.data.relations.TripWithUsers // Убедись, что это то, что возвращает DAO
// import com.example.ridesynq.data.repositories.TripRepository
// import com.example.ridesynq.data.repositories.UserTripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TripViewModel(
    private val tripDao: TripDao,
    private val userTripDao: UserTripDao
) : ViewModel() {

    sealed class CreateTripUiState {
        object Idle : CreateTripUiState()
        object Loading : CreateTripUiState()
        object Success : CreateTripUiState()
        data class Error(val message: String) : CreateTripUiState()
    }
    private val _createTripState = MutableStateFlow<CreateTripUiState>(CreateTripUiState.Idle)
    val createTripState = _createTripState.asStateFlow()


    private val allRelevantTripsWithUsers: Flow<List<TripWithUsers>> =
        tripDao.getAllActiveAndFutureTripsWithUsers()
            .map { tripsWithUsers ->
                val currentTimeMillis = Calendar.getInstance().timeInMillis
                tripsWithUsers.filter { it.trip.datetime >= currentTimeMillis }
            }


    // Поездки, инициированные водителями
    val driverInitiatedTrips: StateFlow<List<TripWithUsers>> =
        allRelevantTripsWithUsers.map { trips ->
            trips.filter { tripWithUsers ->
                tripWithUsers.trip.driverId != null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Поездки, инициированные пассажирами (запросы на поездку)
    val passengerInitiatedTrips: StateFlow<List<TripWithUsers>> =
        allRelevantTripsWithUsers.map { trips ->
            trips.filter { tripWithUsers ->
                tripWithUsers.trip.driverId == null // Если нет водителя, это запрос пассажира
                // ИЛИ, если роль хранится в UserTrip:
                // tripWithUsers.users.all { userInTrip ->
                //    // Проверить, что все пользователи в этой поездке - пассажиры,
                //    // или что создатель поездки - пассажир.
                // }
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
        isToWork: Boolean, // Добавил это поле
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
                    isToWork = isToWork, // Сохраняем направление
                    driverId = if (isDriver) userId else null,
                    seatsAvailable = if (isDriver) seatsAvailable else null,
                    pricePerSeat = if (isDriver) price else null,
                    status = "pending" // Начальный статус
                )


                val tripId = tripDao.insertTripAndGetId(newTrip)

                if (tripId > 0) {
                    val userTrip = UserTrip(
                        userId = userId,
                        tripId = tripId.toInt(),
                        role = if (isDriver) "driver" else "passenger"
                    )

                    userTripDao.insert(userTrip)

                    Log.d("TripViewModel", "Поездка успешно создана: TripID $tripId, UserID $userId, Role: ${userTrip.role}")
                    _createTripState.value = CreateTripUiState.Success
                } else {
                    Log.e("TripViewModel", "Не удалось получить ID созданной поездки (tripId <= 0)")
                    _createTripState.value = CreateTripUiState.Error("Ошибка сохранения поездки")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Ошибка при создании поездки: ${e.message}", e)
                _createTripState.value = CreateTripUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun resetCreateTripState() {
        _createTripState.value = CreateTripUiState.Idle
    }

    // Функции для получения списков поездок (примеры)
    fun getMyDriverTrips(driverId: Int): Flow<List<TripWithUsers>> {
        return tripDao.getTripsByDriver(driverId) // Временно
    }

    fun getMyPassengerTrips(passengerId: Int): Flow<List<TripWithUsers>> {
        return tripDao.getPassengerTripsForUser(passengerId) // Временно
    }

    fun getAllActiveTrips(): Flow<List<TripWithUsers>> { // Или как ты хочешь их называть
        return tripDao.getAllActiveTripsWithUsers() // Временно
    }
}