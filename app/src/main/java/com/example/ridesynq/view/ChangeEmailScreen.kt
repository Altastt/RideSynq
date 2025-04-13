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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.viewmodel.AuthVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeEmailScreen(
    navController: NavController,
    authViewModel: AuthVM
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Basic validation (can be improved)
    val isInputValid = currentPassword.isNotBlank() && newEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_email)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Для смены email введите текущий пароль и новый адрес электронной почты.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Current Password Field
            TextFieldPass( // Reuse your existing password field
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = stringResource(R.string.current_password_placeholder) // Add string resource
            )

            // New Email Field
            TextFieldEmail( // Reuse your existing email field
                value = newEmail,
                onValueChange = { newEmail = it },
                label = stringResource(R.string.new_email_label) // Add string resource
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (currentUser == null) {
                        Toast.makeText(context, "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        val result = authViewModel.changeEmail(
                            newEmail = newEmail.trim(),
                            currentPassword = currentPassword // Pass the entered password
                        )
                        isLoading = false
                        result.onSuccess {
                            Toast.makeText(context, "Email успешно изменен", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // Go back after success
                        }
                        result.onFailure { error ->
                            Toast.makeText(context, "Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = isInputValid && !isLoading && currentUser != null,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.save)) // Add string resource
                }
            }
        }
    }
}