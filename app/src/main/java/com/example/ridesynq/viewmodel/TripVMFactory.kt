package com.example.ridesynq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserTripDao

class TripVMFactory(
    private val tripDao: TripDao,
    private val userTripDao: UserTripDao

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripDao, userTripDao) as T
            // return TripViewModel(tripRepository, userTripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for Trip")
    }
}