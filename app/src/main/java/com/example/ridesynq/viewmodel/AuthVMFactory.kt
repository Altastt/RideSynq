package com.example.ridesynq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ridesynq.data.repositories.UserRepository

class AuthVMFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthVM(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}