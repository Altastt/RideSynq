package com.example.ridesynq.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.ridesynq.BuildConfig
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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
    private val _currentUser = MutableStateFlow<User?>(null) // Используем StateFlow
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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
                    transport_number = null,
                    transport_color = null,
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
            val user = userRepository.validateCredentials(login, password) // TODO: Сравнение хэша
            user?.let {
                _currentUser.value = it // <<<<<<< Устанавливаем текущего пользователя
                Result.success(it)
            } ?: run {
                _currentUser.value = null // Сбрасываем, если вход не удался
                Result.failure(Exception("Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Log.e("AuthVM", "Login failed", e)
            _currentUser.value = null // Сбрасываем при ошибке
            Result.failure(Exception("Ошибка входа: ${e.message}", e))
        }
    }
    fun logoutUser() {
        _currentUser.value = null
        // Здесь можно добавить очистку токенов, сессий и т.д., если они используются
    }

    suspend fun updateUserCarDetails(
        makeModel: String?, // Объединяем Марку и Модель
        number: String?,
        color: String?
    ): Boolean { // Возвращаем true при успехе, false при ошибке
        val currentUserValue = _currentUser.value ?: return false // Нужен текущий пользователь
        return try {
            val updatedUser = currentUserValue.copy(
                transport_name = makeModel?.trim()?.takeIf { it.isNotEmpty() },
                transport_number = number?.trim()?.takeIf { it.isNotEmpty() },
                transport_color = color?.trim()?.takeIf { it.isNotEmpty() }
            )
            // Выполняем обновление в IO диспатчере
            withContext(Dispatchers.IO) {
                userRepository.updateUser(updatedUser)
            }
            // Обновляем StateFlow в основном потоке
            _currentUser.value = updatedUser
            true
        } catch (e: Exception) {
            Log.e("AuthVM", "Failed to update car details", e)
            false
        }
    }
}