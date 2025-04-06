package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Импорт для скролла
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // Импорт для скролла
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText
// Импортируем обновленные компоненты
import com.example.ridesynq.view.TextFieldEmail
import com.example.ridesynq.view.TextFieldPass
import com.example.ridesynq.view.navigation.AuthScreen
import com.example.ridesynq.view.navigation.GraphRoute
import com.example.ridesynq.viewmodel.AuthVM
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthVM,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val userNotFoundMessage = stringResource(R.string.user_not_found)
    val scrollState = rememberScrollState() // Состояние для скролла

    Column(
        modifier = Modifier
            .fillMaxSize() // Заполняем весь экран
            .systemBarsPadding() // Отступы от системных баров (статус бар, навигация)
            .imePadding() // Отступ для клавиатуры
            .verticalScroll(scrollState) // Делаем колонку скроллящейся
            .padding(horizontal = 16.dp, vertical = 24.dp), // Общие отступы внутри скролла
        horizontalAlignment = Alignment.CenterHorizontally,
        // Убираем verticalArrangement = Arrangement.Center, т.к. скролл сам распределит контент
    ) {
        Spacer(modifier = Modifier.height(60.dp)) // Отступ сверху для заголовка

        AutoresizedText(
            stringResource(R.string.sign_in_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 60.dp) // Уменьшим отступ
        )

        // Используем обновленный TextFieldEmail
        TextFieldEmail(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = stringResource(R.string.sign_in_email)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Стандартный отступ

        // Используем обновленный TextFieldPass
        TextFieldPass(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            placeholder = stringResource(R.string.sign_in_password) // placeholder остается в TextFieldPass
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // ... (логика входа без изменений) ...
                val email = emailState.value.trim()
                val password = passwordState.value

                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Введите email и пароль", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                coroutineScope.launch {
                    val result = authViewModel.loginUser(email, password)
                    result.onSuccess { user ->
                        Toast.makeText(context, "Вход успешен!", Toast.LENGTH_SHORT).show()
                        val destination = if (user.post_id == 0) { // Администратор
                            GraphRoute.ADMIN_MAIN // Убедитесь, что этот роут существует
                        } else { // Обычный пользователь
                            GraphRoute.MAIN
                        }
                        navController.navigate(GraphRoute.MAIN) {
                            popUpTo(GraphRoute.AUTHENTICATION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }.onFailure { exception ->
                        Toast.makeText(context, userNotFoundMessage, Toast.LENGTH_LONG).show()
                        navController.navigate(AuthScreen.Registration.route) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            shape = RoundedCornerShape(100), // Полностью скругленная кнопка
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 32.dp) // Больше отступы для акцента
        ) {
            AutoresizedText(
                stringResource(R.string.sign_in_button),
                style = MaterialTheme.typography.labelLarge // Чуть крупнее текст кнопки
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text( // Используем обычный Text
                stringResource(R.string.sign_in_text_to_signup),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 4.dp) // Меньше отступ
            )
            Text(
                stringResource(R.string.sign_in_tb_to_signup),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clickable {
                    navController.navigate(AuthScreen.Registration.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp)) // Отступ снизу
    }
}