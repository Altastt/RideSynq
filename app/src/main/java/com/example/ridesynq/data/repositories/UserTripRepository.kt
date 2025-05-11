package com.example.ridesynq.data.repositories

import com.example.ridesynq.data.dao.UserTripDao
import com.example.ridesynq.data.entities.UserTrip

class UserTripRepository(private val userTripDao: UserTripDao) {

    suspend fun insertUserTrip(userTrip: UserTrip) {
        userTripDao.insert(userTrip)
    }
}