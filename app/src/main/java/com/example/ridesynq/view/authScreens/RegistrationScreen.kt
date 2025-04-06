package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Импорт
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // Импорт
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.BackButton
// Импортируем обновленные компоненты
import com.example.ridesynq.view.TextFieldCustom
import com.example.ridesynq.view.TextFieldEmail
import com.example.ridesynq.view.TextFieldPass
import com.example.ridesynq.view.navigation.AuthScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthVM,
    companyViewModel: CompanyViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val secondPasswordState = remember { mutableStateOf("") }
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val surname = remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState() // Состояние скролла

    // ... (логика сравнения паролей и состояние dropdown без изменений) ...
    val passwordsMatch = remember(passwordState.value, secondPasswordState.value) {
        secondPasswordState.value.isEmpty() || passwordState.value == secondPasswordState.value
    }
    val displayPasswordError = secondPasswordState.value.isNotEmpty() && !passwordsMatch
    val companies by companyViewModel.allCompanies.collectAsState(initial = emptyList())
    var companyDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(scrollState) // <<<<<<< ДОБАВЛЯЕМ СКРОЛЛ
            .padding(horizontal = 16.dp), // Отступы по бокам
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Отступ сверху для кнопки Back
        Row(
            modifier = Modifier.fillMaxWidth()
            // .padding(bottom = 20.dp) // Убираем нижний отступ здесь
        ) { BackButton(navController) }

        Spacer(modifier = Modifier.height(16.dp)) // Отступ после кнопки

        AutoresizedText(
            stringResource(R.string.sign_up_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 32.dp) // Увеличим отступ
        )

        // Используем обновленный TextFieldCustom
        TextFieldCustom(
            value = lastName.value,
            onValueChange = { lastName.value = it },
            label = stringResource(R.string.sign_up_lastname)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Стандартный отступ 16dp
        TextFieldCustom(
            value = firstName.value,
            onValueChange = { firstName.value = it },
            label = stringResource(R.string.sign_up_firstname)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldCustom(
            value = surname.value,
            onValueChange = { surname.value = it },
            label = stringResource(R.string.sign_up_surname)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Выбор компании (ExposedDropdownMenuBox уже использует OutlinedTextField)
        ExposedDropdownMenuBox(
            expanded = companyDropdownExpanded,
            onExpandedChange = { companyDropdownExpanded = !companyDropdownExpanded },
            modifier = Modifier.fillMaxWidth() // Растягиваем по ширине
        ) {
            // Используем стандартный OutlinedTextField внутри для консистентности
            OutlinedTextField(
                value = selectedCompany?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.select_company)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(), // Явно указываем fillMaxWidth
                shape = RoundedCornerShape(12.dp) // Применяем стандартную форму
            )
            ExposedDropdownMenu(
                expanded = companyDropdownExpanded,
                onDismissRequest = { companyDropdownExpanded = false }
            ) {
                // ... (содержимое меню без изменений) ...
                if (companies.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_companies_available)) },
                        onClick = { companyDropdownExpanded = false },
                        enabled = false
                    )
                } else {
                    companies.forEach { company ->
                        DropdownMenuItem(
                            text = { Text(company.name) },
                            onClick = {
                                selectedCompany = company
                                companyDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Используем обновленный TextFieldEmail
        TextFieldEmail(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = stringResource(R.string.sign_in_email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Используем обновленный TextFieldPass
        TextFieldPass(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            placeholder = stringResource(R.string.sign_in_password)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Используем обновленный TextFieldPass для второго пароля
        TextFieldPass(
            value = secondPasswordState.value,
            onValueChange = { secondPasswordState.value = it },
            placeholder = stringResource(R.string.sign_in_retrypassword),
            isError = displayPasswordError,
            supportingText = {
                if (displayPasswordError) {
                    Text(
                        text = "Пароли не совпадают",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // Checkbox и текст согласия
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp), // Отступы сверху/снизу
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
                // modifier = Modifier.size(24.dp) // Размер по умолчанию
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text( // Используем обычный Text
                stringResource(R.string.sign_up_confidence),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Кнопка Регистрации
        val isButtonEnabled = firstName.value.isNotBlank() &&
                lastName.value.isNotBlank() &&
                emailState.value.isNotBlank() &&
                passwordState.value.isNotBlank() &&
                secondPasswordState.value.isNotBlank() &&
                passwordsMatch &&
                checked &&
                selectedCompany != null

        Button(
            onClick = {
                // ... (логика регистрации без изменений) ...
                if (!isButtonEnabled) {
                    Toast.makeText(context, "Пожалуйста, заполните все поля корректно и выберите компанию", Toast.LENGTH_LONG).show()
                    return@Button
                }
                coroutineScope.launch {
                    val result = authViewModel.registerUser(
                        firstName = firstName.value.trim(),
                        lastName = lastName.value.trim(),
                        surname = surname.value.trim().takeIf { it.isNotEmpty() },
                        login = emailState.value.trim(),
                        password = passwordState.value,
                        companyId = selectedCompany!!.id
                    )
                    when (result) {
                        is AuthVM.RegistrationResult.Success -> {
                            Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                            navController.navigate(AuthScreen.Login.route) {
                                popUpTo(AuthScreen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is AuthVM.RegistrationResult.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            enabled = isButtonEnabled,
            shape = RoundedCornerShape(100), // Полностью скругленная
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 10.dp) // Отступ снизу до след. кнопки
        ) {
            AutoresizedText(
                stringResource(R.string.sign_up_button),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Кнопка Регистрации Компании (можно использовать OutlinedButton для контраста)
        OutlinedButton( // Используем OutlinedButton
            onClick = {
                navController.navigate(AuthScreen.CompanyRegistration.route)
            },
            shape = RoundedCornerShape(100), // Полностью скругленная
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            AutoresizedText(
                stringResource(R.string.sign_up_company_button),
                style = MaterialTheme.typography.labelLarge // Тот же стиль текста
            )
        }
        Spacer(modifier = Modifier.height(32.dp)) // Отступ в конце скролла
    }
}