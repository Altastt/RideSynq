package com.example.ridesynq.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
// import androidx.lifecycle.LiveData // Убрали
// import androidx.lifecycle.MutableLiveData // Убрали
import androidx.lifecycle.ViewModel
import com.example.ridesynq.BuildConfig
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.repositories.UserRepository
import java.time.LocalTime

class AuthVM(private val userRepository: UserRepository) : ViewModel() {
    init {
        if (BuildConfig.DEBUG) {
            Log.d("AuthVM", "ViewModel initialized")
        }
    }
    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
    }

    // Эти состояния могут быть полезны для других целей, оставим их, если они нужны где-то еще
    private val _departureTime = mutableStateOf<LocalTime?>(null)
    val departureTime: State<LocalTime?> = _departureTime

    fun setDepartureTime(time: LocalTime) {
        _departureTime.value = time
    }


    // Регистрация - теперь принимает companyId
    suspend fun registerUser(
        firstName: String?, // Nullable
        lastName: String?, // Nullable
        surname: String?,
        login: String,
        password: String,
        companyId: Int,
        isAdmin: Boolean = false // Флаг админа
    ): RegistrationResult {
        // Базовая валидация
        if (login.isBlank() || password.isBlank() || companyId <= 0) {
            return RegistrationResult.Error("Email, пароль и ID компании обязательны")
        }
        // Валидация для обычного пользователя
        if (!isAdmin && (firstName.isNullOrBlank() || lastName.isNullOrBlank())) {
            return RegistrationResult.Error("Имя и фамилия обязательны для пользователя")
        }

        return try {
            if (userRepository.isLoginUnique(login)) {
                val user = User(
                    firstname = firstName?.trim(),
                    lastname = lastName?.trim(),
                    surname = surname?.trim()?.takeIf { it.isNotEmpty() },
                    login = login.trim(),
                    password = password, // Хэшировать здесь!
                    company_id = companyId,
                    post_id = if (isAdmin) 0 else 1, // Используем 0 для админа, 1 для обычного (или другой ID)
                    phone = null, // Не запрашиваем при регистрации
                    transport_name = null,
                    transport_number = null
                )
                userRepository.createUser(user)
                RegistrationResult.Success
            } else {
                RegistrationResult.Error("Пользователь с таким email уже существует")
            }
        } catch (e: Exception) {
            Log.e("AuthVM", "Registration failed", e)
            RegistrationResult.Error(e.message ?: "Неизвестная ошибка регистрации")
        }
    }

    // Авторизация
    suspend fun loginUser(login: String, password: String): Result<User> {
        if (login.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email и пароль не могут быть пустыми"))
        }
        return try {
            // TODO: При реальном использовании пароль должен сравниваться с хэшем в БД
            val user = userRepository.validateCredentials(login, password)
            user?.let {
                // Пользователь найден
                Result.success(it)
            } ?: Result.failure(Exception("Неверный email или пароль")) // Более общее сообщение об ошибке
        } catch (e: Exception) {
            Log.e("AuthVM", "Login failed", e)
            Result.failure(Exception("Ошибка входа: ${e.message}", e))
        }
    }
}