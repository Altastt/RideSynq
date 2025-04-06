package com.example.ridesynq.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.viewmodel.AuthVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    navController: NavController,
    authViewModel: AuthVM
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Состояния для полей ввода, инициализируем текущими данными пользователя
    var makeModel by remember(currentUser) { mutableStateOf(currentUser?.transport_name ?: "") }
    var number by remember(currentUser) { mutableStateOf(currentUser?.transport_number ?: "") }
    var color by remember(currentUser) { mutableStateOf(currentUser?.transport_color ?: "") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Данные автомобиля") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) // Отступы для контента
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = makeModel,
                onValueChange = { makeModel = it },
                label = { Text("Марка и модель") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Регистрационный номер") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Цвет") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val success = authViewModel.updateUserCarDetails(
                            makeModel = makeModel,
                            number = number,
                            color = color
                        )
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Данные автомобиля сохранены", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LocalContentColor.current)
                } else {
                    Text("Сохранить")
                }
            }
        }
    }
}