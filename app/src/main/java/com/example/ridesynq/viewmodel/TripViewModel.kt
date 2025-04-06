package com.example.ridesynq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.ridesynq.data.repositories.TripRepository // Нужен репозиторий
import kotlinx.coroutines.launch

class TripViewModel(/*private val tripRepository: TripRepository*/) : ViewModel() {

    fun createTripRequest(
        userId: Int,
        companyId: Int,
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        dateTimeMillis: Long,
        isToWork: Boolean // Можете использовать Enum TripDirection
    ) {
        viewModelScope.launch {
            // TODO:
            // 1. Создать объект Trip (возможно, с координатами старта/конца?)
            // 2. Вставить Trip через tripRepository -> tripDao
            // 3. Получить ID созданного Trip
            // 4. Создать объект UserTrip (связь пользователя и поездки)
            // 5. Вставить UserTrip через репозиторий/DAO
            println("Trip request received: User=$userId, Company=$companyId, Start=($startLat, $startLon), End=($endLat, $endLon), Time=$dateTimeMillis, ToWork=$isToWork")
        }
    }
}
