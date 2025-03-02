package com.example.ridesynq.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalTime

class AuthVM : ViewModel() {


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
/*
    suspend fun registration(email: String, password: String,) { // authApi: AuthApi) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    authApi.userRegistration(
                        RegistrationModel(
                            email,
                            password
                        )
                    )
                }
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

    suspend fun authorization(email: String, password: String,) { //authApi: AuthApi) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                withContext(Dispatchers.Main) {
                   // val userToken = authApi.userLogin(
                     //   RegistrationModel(
                     //       email,
                      //      password
                      //  )

                   // )
                  //  Log.d("ВЕРНИ ТОКЕН ЗАРАЗА", userToken.token)
                    // Установка токена после успешной аутентификации
                   // setToken(userToken.token)
                }
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

*/
}