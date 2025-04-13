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
fun ChangePasswordScreen(
    navController: NavController,
    authViewModel: AuthVM
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Basic validation
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val displayPasswordError = confirmPassword.isNotEmpty() && !passwordsMatch
    val isInputValid = currentPassword.isNotBlank() && newPassword.isNotBlank() && passwordsMatch // Add strength check later

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_password)) },
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
                text = "Для смены пароля введите текущий пароль и новый пароль дважды.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Current Password
            TextFieldPass(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = stringResource(R.string.current_password_placeholder) // Reuse string
            )

            // New Password
            TextFieldPass(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = stringResource(R.string.new_password_placeholder) // Add string
            )

            // Confirm New Password
            TextFieldPass(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(R.string.confirm_new_password_placeholder), // Add string
                isError = displayPasswordError,
                supportingText = {
                    if (displayPasswordError) {
                        Text("Пароли не совпадают", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
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
                        val result = authViewModel.changePassword(
                            currentPassword = currentPassword, // Pass entered current pw
                            newPassword = newPassword // Pass the new pw
                        )
                        isLoading = false
                        result.onSuccess {
                            Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
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
                    Text(stringResource(R.string.save)) // Reuse string
                }
            }
        }
    }
}