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

    suspend fun updateUserCarDetails(makeModel: String?, number: String?, color: String?): Boolean {
        val currentUserValue = _currentUser.value ?: return false
        return try {
            val updatedUser = currentUserValue.copy(
                transport_name = makeModel?.trim()?.takeIf { it.isNotEmpty() },
                transport_number = number?.trim()?.takeIf { it.isNotEmpty() },
                transport_color = color?.trim()?.takeIf { it.isNotEmpty() }
            )
            withContext(Dispatchers.IO) { userRepository.updateUser(updatedUser) }
            _currentUser.value = updatedUser
            true
        } catch (e: Exception) {
            Log.e("AuthVM", "Failed to update car details", e)
            false
        }
    }

    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<Unit> {
        val currentUserValue = _currentUser.value ?: return Result.failure(Exception("Пользователь не авторизован"))
        if (newEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            return Result.failure(Exception("Некорректный новый email"))
        }
        if (currentPassword.isBlank()) {
            return Result.failure(Exception("Введите текущий пароль"))
        }

        return try {
            // 1. Verify current password
            val userFromDb = withContext(Dispatchers.IO) {
                userRepository.validateCredentials(currentUserValue.login, currentPassword) // Use validateCredentials
            }
            // Explicit null check before accessing id
            if (userFromDb == null) {
                return Result.failure(Exception("Неверный текущий пароль"))
            }
            // Now we know userFromDb is not null, accessing id is safe
            if (userFromDb.id != currentUserValue.id) {
                // This case might be redundant if validateCredentials works correctly, but belt-and-suspenders
                return Result.failure(Exception("Ошибка проверки пользователя"))
            }


            // 2. Check if new email is unique (excluding current user)
            val isTaken = withContext(Dispatchers.IO) {
                userRepository.doesLoginExist(newEmail) // <<< USE CORRECT REPO METHOD
            }
            if (isTaken && newEmail != currentUserValue.login) {
                return Result.failure(Exception("Этот email уже используется"))
            }

            // 3. Update user
            val updatedUser = currentUserValue.copy(login = newEmail)
            withContext(Dispatchers.IO) {
                userRepository.updateUser(updatedUser)
            }
            _currentUser.value = updatedUser
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("AuthVM", "Change Email failed", e)
            Result.failure(Exception("Ошибка смены email: ${e.message}", e))
        }
    }


    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val currentUserValue = _currentUser.value ?: return Result.failure(Exception("Пользователь не авторизован"))

        if (currentPassword.isBlank() || newPassword.isBlank()) {
            return Result.failure(Exception("Пароли не могут быть пустыми"))
        }

        return try {
            // 1. Verify current password
            val userFromDb = withContext(Dispatchers.IO) {
                userRepository.validateCredentials(currentUserValue.login, currentPassword) // Use validateCredentials
            }
            // Explicit null check
            if (userFromDb == null) {
                return Result.failure(Exception("Неверный текущий пароль"))
            }
            // Check ID just in case
            if (userFromDb.id != currentUserValue.id) {
                return Result.failure(Exception("Ошибка проверки пользователя"))
            }

            // 2. Update user with new password
            val updatedUser = currentUserValue.copy(password = newPassword) // HASH newPassword!
            withContext(Dispatchers.IO) {
                userRepository.updateUser(updatedUser)
            }
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("AuthVM", "Change Password failed", e)
            Result.failure(Exception("Ошибка смены пароля: ${e.message}", e))
        }
    }

    suspend fun updateUserProfile(updatedUser: User): Result<Unit> {
        val currentUserValue = _currentUser.value ?: return Result.failure(Exception("Пользователь не авторизован"))
        if (updatedUser.id != currentUserValue.id) {
            return Result.failure(Exception("Несоответствие ID пользователя"))
        }
        val finalUserToSave = updatedUser.copy(
            password = currentUserValue.password,
            login = currentUserValue.login
        )
        return try {
            withContext(Dispatchers.IO) { userRepository.updateUser(finalUserToSave) }
            _currentUser.value = finalUserToSave
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthVM", "Update Profile failed", e)
            Result.failure(Exception("Ошибка обновления профиля: ${e.message}", e))
        }
    }

    suspend fun updateUserAvatarPath(avatarPath: String?): Result<Unit> {
        val currentUserValue = _currentUser.value ?: return Result.failure(Exception("Пользователь не авторизован"))
        return try {
            val updatedUser = currentUserValue.copy(avatarUrl = avatarPath)
            withContext(Dispatchers.IO) {
                userRepository.updateUser(updatedUser)
            }
            _currentUser.value = updatedUser
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthVM", "Update Avatar Path failed", e)
            Result.failure(Exception("Ошибка обновления аватара: ${e.message}", e))
        }
    }


}