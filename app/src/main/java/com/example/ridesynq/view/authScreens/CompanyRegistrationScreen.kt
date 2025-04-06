package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.BackButton
import com.example.ridesynq.view.TextFieldCustom
import com.example.ridesynq.view.TextFieldEmail
import com.example.ridesynq.view.TextFieldPass
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel
import kotlinx.coroutines.launch

@Composable
fun CompanyRegistrationScreen(
    navController: NavController,
    companyViewModel: CompanyViewModel,
    authViewModel: AuthVM
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val companyName = remember { mutableStateOf("") }
    val inn = remember { mutableStateOf("") }
    val kpp = remember { mutableStateOf("") }
    val ogrn = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }
    val longitude = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val secondPasswordState = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val passwordsMatch = remember(passwordState.value, secondPasswordState.value) {
        // Считаем совпавшими, только если оба не пустые и равны
        passwordState.value.isNotEmpty() && passwordState.value == secondPasswordState.value
    }
    // Ошибка отображается, если второе поле не пустое, но пароли не совпадают
    val displayPasswordError = secondPasswordState.value.isNotEmpty() && !passwordsMatch

    var isLoading by remember { mutableStateOf(false) } // Состояние загрузки для кнопки

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) { BackButton(navController) }
        Spacer(modifier = Modifier.height(16.dp))

        AutoresizedText(
            stringResource(R.string.sign_up_company_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- Поля компании (без изменений) ---
        TextFieldCustom(value = companyName.value, onValueChange = { companyName.value = it }, label = stringResource(R.string.sign_up_company_name))
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldCustom(value = inn.value, onValueChange = { inn.value = it }, label = stringResource(R.string.sign_up_company_inn), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldCustom(value = kpp.value, onValueChange = { kpp.value = it }, label = stringResource(R.string.sign_up_company_kpp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldCustom(value = ogrn.value, onValueChange = { ogrn.value = it }, label = stringResource(R.string.sign_up_company_ogrn), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(16.dp))
        val isLatitudeError = latitude.value.isNotBlank() && !isValidCoordinate(latitude.value, 90.0)
        TextFieldCustom(value = latitude.value, onValueChange = { newValue -> if (newValue.matches(Regex("^-?\\d*\\.?\\d*\$"))) latitude.value = newValue }, label = stringResource(R.string.sign_up_company_latitude), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = isLatitudeError, supportingText = { if (isLatitudeError) Text("Диапазон: от -90.0 до 90.0", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) })
        Spacer(modifier = Modifier.height(16.dp))
        val isLongitudeError = longitude.value.isNotBlank() && !isValidCoordinate(longitude.value, 180.0)
        TextFieldCustom(value = longitude.value, onValueChange = { newValue -> if (newValue.matches(Regex("^-?\\d*\\.?\\d*\$"))) longitude.value = newValue }, label = stringResource(R.string.sign_up_company_longitude), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = isLongitudeError, supportingText = { if (isLongitudeError) Text("Диапазон: от -180.0 до 180.0", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) })
        Spacer(modifier = Modifier.height(24.dp))
        // --- Конец полей компании ---

        // --- Обязательные поля администратора ---
        Text(
            "Данные администратора компании",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextFieldEmail(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = stringResource(R.string.sign_in_email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldPass(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            placeholder = stringResource(R.string.sign_in_password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldPass(
            value = secondPasswordState.value,
            onValueChange = { secondPasswordState.value = it },
            placeholder = stringResource(R.string.sign_in_retrypassword),
            isError = displayPasswordError,
            supportingText = {
                if (displayPasswordError) { // Показываем ошибку, если второе поле введено и не совпадает
                    Text("Пароли не совпадают", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
        // --- Конец полей админа ---

        Spacer(modifier = Modifier.height(32.dp))

        // Валидация для кнопки
        val isCompanyDataValid = companyName.value.isNotBlank() &&
                inn.value.isNotBlank() &&
                isValidCoordinate(latitude.value, 90.0) &&
                isValidCoordinate(longitude.value, 180.0)
        val isAdminDataValid = emailState.value.isNotBlank() &&
                passwordState.value.isNotBlank() &&
                secondPasswordState.value.isNotBlank() &&
                passwordsMatch
        val isButtonEnabled = isCompanyDataValid && isAdminDataValid && !isLoading

        Button(
            onClick = {

                if (!isCompanyDataValid) {
                    Toast.makeText(context, "Проверьте правильность ввода данных компании", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isAdminDataValid) {
                    Toast.makeText(context, "Проверьте правильность ввода email и пароля администратора", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                coroutineScope.launch {
                    // 1. Создаем компанию
                    val newCompany = Company(
                        name = companyName.value.trim(),
                        inn = inn.value.trim(),
                        kpp = kpp.value.trim(),
                        ogrn = ogrn.value.trim(),
                        latitude = latitude.value.toDouble(),
                        longitude = longitude.value.toDouble()
                    )
                    val companyIdLong = companyViewModel.insertCompanyAndGetId(newCompany)

                    if (companyIdLong != null && companyIdLong > 0) {
                        val companyIdInt = companyIdLong.toInt()
                        // Вызов registerUser для админа
                        val registrationResult = authViewModel.registerUser(
                            firstName = null,
                            lastName = null,
                            surname = null,
                            login = emailState.value.trim(),
                            password = passwordState.value,
                            companyId = companyIdInt,
                            isAdmin = true
                        )

                        when (registrationResult) {
                            is AuthVM.RegistrationResult.Success -> {
                                Toast.makeText(context, "Компания и администратор успешно зарегистрированы!", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            }
                            is AuthVM.RegistrationResult.Error -> {
                                Toast.makeText(context, "Компания создана, но ошибка регистрации администратора: ${registrationResult.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Ошибка при создании компании", Toast.LENGTH_LONG).show()
                    }
                    isLoading = false // Завершаем загрузку в любом случае
                }
            },
            enabled = isButtonEnabled, // Используем вычисленное значение
            shape = RoundedCornerShape(100),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // Цвет индикатора на кнопке
                )
            } else {
                AutoresizedText(
                    stringResource(R.string.sign_up_company_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// isValidCoordinate остается без изменений
fun isValidCoordinate(value: String, maxRange: Double): Boolean {
    if (value.isBlank()) return false // Пустое значение теперь невалидно
    return try {
        val doubleValue = value.toDouble()
        doubleValue in -maxRange..maxRange
    } catch (e: NumberFormatException) {
        false // Нечисловое значение невалидно
    }
}