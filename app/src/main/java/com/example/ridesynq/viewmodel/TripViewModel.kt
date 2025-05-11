package com.example.ridesynq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.dao.TripDao         // Временно, для прямого доступа
import com.example.ridesynq.data.dao.UserTripDao     // Временно, для прямого доступа
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.UserTrip
// import com.example.ridesynq.data.repositories.TripRepository // Предпочтительно
// import com.example.ridesynq.data.repositories.UserTripRepository // Предпочтительно
import com.example.ridesynq.data.relations.TripWithUsers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel(
    private val tripDao: TripDao,         // Замени на tripRepository
    private val userTripDao: UserTripDao     // Замени на userTripRepository
    // private val tripRepository: TripRepository,
    // private val userTripRepository: UserTripRepository
) : ViewModel() {

    // Пример Flow для отображения в TripScreen
    // Это можно сделать более сложным, с фильтрацией и т.д.
    // val activeTrips: Flow<List<TripWithUsers>> = tripRepository.getAllActiveTripsWithUsers() // Когда будет репозиторий

    sealed class CreateTripUiState {
        object Idle : CreateTripUiState()
        object Loading : CreateTripUiState()
        object Success : CreateTripUiState()
        data class Error(val message: String) : CreateTripUiState()
    }
    private val _createTripState = MutableStateFlow<CreateTripUiState>(CreateTripUiState.Idle)
    val createTripState = _createTripState.asStateFlow()

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

                // val tripId = tripRepository.insertTripAndGetId(newTrip)
                val tripId = tripDao.insertTripAndGetId(newTrip) // Используем DAO напрямую (временно)

                if (tripId > 0) {
                    val userTrip = UserTrip(
                        userId = userId,
                        tripId = tripId.toInt(),
                        role = if (isDriver) "driver" else "passenger"
                    )
                    // userTripRepository.insertUserTrip(userTrip)
                    userTripDao.insert(userTrip) // Используем DAO напрямую (временно)

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
        // return tripRepository.getTripsByDriver(driverId)
        return tripDao.getTripsByDriver(driverId) // Временно
    }

    fun getMyPassengerTrips(passengerId: Int): Flow<List<TripWithUsers>> {
        // return tripRepository.getPassengerTripsForUser(passengerId)
        return tripDao.getPassengerTripsForUser(passengerId) // Временно
    }

    fun getAllActiveTrips(): Flow<List<TripWithUsers>> { // Или как ты хочешь их называть
        // return tripRepository.getAllActiveTripsWithUsers()
        return tripDao.getAllActiveTripsWithUsers() // Временно
    }
}