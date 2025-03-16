package com.example.ridesynq.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.repositories.UserRepository
import java.time.LocalTime

class AuthVM(private val userRepository: UserRepository) : ViewModel() {


    private val _emailState = MutableLiveData<String>()
    val emailState: LiveData<String> = _emailState
    private val _passwordState = MutableLiveData<String>()
    val passwordState: LiveData<String> = _passwordState
    private val _secondPasswordState = MutableLiveData<String>()
    val secondPasswordState: LiveData<String> = _secondPasswordState
    private val _departureTime = mutableStateOf<LocalTime?>(null)
    val departureTime: State<LocalTime?> = _departureTime

    fun setDepartureTime(time: LocalTime) {
        _departureTime.value = time
    }

    private val _tokenState = MutableLiveData<String>()
    val tokenState: LiveData<String> = _tokenState

    fun checkPasswordMatch(password: String, secondPassword: String?): Boolean {
        return if (secondPassword != null) {
            password == secondPassword
        } else {
            true
        }
    }
    private fun setToken(token: String) {
        _tokenState.value = token
    }
    // Регистрация
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        surname: String?,
        login: String,
        password: String
    ): Result<Unit> {
        return try {
            if (userRepository.isLoginUnique(login)) {
                val user = User(
                    firstname = firstName,
                    lastname = lastName,
                    surname = surname,
                    login = login,
                    password = password,
                    company_id = 1, // Временные значения
                    post_id = 1,
                    phone = "",
                    transport_name = null,
                    transport_number = null
                )
                userRepository.createUser(user)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Логин уже занят"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Авторизация
    suspend fun loginUser(login: String, password: String): Result<User> {
        return try {
            val user = userRepository.validateCredentials(login, password)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Неверные учетные данные"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}