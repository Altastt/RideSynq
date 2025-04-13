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
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.viewmodel.AuthVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthVM
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val originalUser by authViewModel.currentUser.collectAsState() // Get the current user

    // State holders for editable fields, initialized from originalUser
    var firstName by remember(originalUser) { mutableStateOf(originalUser?.firstname ?: "") }
    var lastName by remember(originalUser) { mutableStateOf(originalUser?.lastname ?: "") }
    var surname by remember(originalUser) { mutableStateOf(originalUser?.surname ?: "") }
    var phone by remember(originalUser) { mutableStateOf(originalUser?.phone ?: "") }
    var homeStreet by remember(originalUser) { mutableStateOf(originalUser?.homeAddressStreet ?: "") }
    var homeCity by remember(originalUser) { mutableStateOf(originalUser?.homeAddressCity ?: "") }
    var homePostalCode by remember(originalUser) { mutableStateOf(originalUser?.homeAddressPostalCode ?: "") }
    // Add latitude/longitude if you have input methods for them

    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) }, // Add string
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (originalUser == null) {
            // Show loading or error if user data isn't available yet
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Загрузка данных пользователя...") // Or CircularProgressIndicator
            }
        } else {
            // Form content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Personal Info ---
                Text("Личная информация", style = MaterialTheme.typography.titleMedium)
                TextFieldCustom(value = lastName, onValueChange = { lastName = it }, label = stringResource(R.string.sign_up_lastname))
                TextFieldCustom(value = firstName, onValueChange = { firstName = it }, label = stringResource(R.string.sign_up_firstname))
                TextFieldCustom(value = surname, onValueChange = { surname = it }, label = stringResource(R.string.sign_up_surname))
                TextFieldCustom(value = phone, onValueChange = { phone = it }, label = stringResource(R.string.phone_number_label)) // Add string

                Spacer(modifier = Modifier.height(16.dp))

                // --- Home Address ---
                Text("Домашний адрес", style = MaterialTheme.typography.titleMedium)
                TextFieldCustom(value = homeCity, onValueChange = { homeCity = it }, label = stringResource(R.string.city_label)) // Add string
                TextFieldCustom(value = homeStreet, onValueChange = { homeStreet = it }, label = stringResource(R.string.street_address_label)) // Add string
                TextFieldCustom(value = homePostalCode, onValueChange = { homePostalCode = it }, label = stringResource(R.string.postal_code_label)) // Add string
                // TODO: Add map view or fields for Lat/Lng if needed

                Spacer(modifier = Modifier.height(32.dp))

                // --- Save Button ---
                Button(
                    onClick = {
                        val updatedUser = originalUser?.copy(
                            firstname = firstName.trim().takeIf { it.isNotEmpty() },
                            lastname = lastName.trim().takeIf { it.isNotEmpty() },
                            surname = surname.trim().takeIf { it.isNotEmpty() },
                            phone = phone.trim().takeIf { it.isNotEmpty() },
                            homeAddressStreet = homeStreet.trim().takeIf { it.isNotEmpty() },
                            homeAddressCity = homeCity.trim().takeIf { it.isNotEmpty() },
                            homeAddressPostalCode = homePostalCode.trim().takeIf { it.isNotEmpty() }
                            // Update lat/lng if applicable
                        )

                        if (updatedUser != null) {
                            isLoading = true
                            coroutineScope.launch {
                                val result = authViewModel.updateUserProfile(updatedUser)
                                isLoading = false
                                result.onSuccess {
                                    Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                result.onFailure { error ->
                                    Toast.makeText(context, "Ошибка обновления: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Не удалось создать обновленные данные", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading && originalUser != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}