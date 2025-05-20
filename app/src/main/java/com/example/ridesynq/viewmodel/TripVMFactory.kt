package com.example.ridesynq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserTripDao

class TripVMFactory(
    private val tripDao: TripDao,
    private val userTripDao: UserTripDao,
    private val authVM: AuthVM // Добавляем AuthVM
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripDao, userTripDao, authVM) as T // Передаем AuthVM
        }
        throw IllegalArgumentException("Unknown ViewModel class for Trip")
    }
}