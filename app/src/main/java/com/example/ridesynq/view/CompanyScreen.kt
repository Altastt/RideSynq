package com.example.ridesynq.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(
    navController: NavController,
    authViewModel: AuthVM,        // Нужен для ID компании пользователя
    companyViewModel: CompanyViewModel // Нужен для загрузки компании
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val selectedCompany by companyViewModel.selectedCompany.collectAsState()

    // Загружаем компанию, когда известен ID компании пользователя
    LaunchedEffect(currentUser) {
        currentUser?.company_id?.let { companyId ->
            companyViewModel.loadCompanyById(companyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.company_info_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
        ) {
            when {
                // Показываем данные, если компания загружена
                selectedCompany != null -> {
                    CompanyInfoCard(company = selectedCompany!!) { company ->
                        // TODO: Навигация на карту с меткой компании
                        // Нужен отдельный экран карты или передача координат
                        // Пример: navController.navigate("map_screen/${company.latitude}/${company.longitude}")
                        println("Navigate to map for company: ${company.name} at ${company.latitude}, ${company.longitude}")
                    }
                }
                // Показываем индикатор загрузки, пока компания не загружена (и пользователь известен)
                currentUser != null && selectedCompany == null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                // Сообщение, если пользователь или его компания не найдены
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            stringResource(R.string.company_not_found),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyInfoCard(
    company: com.example.ridesynq.data.entities.Company,
    onLocationClick: (com.example.ridesynq.data.entities.Company) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CompanyInfoRow(label = stringResource(R.string.sign_up_company_inn), value = company.inn)
            CompanyInfoRow(label = stringResource(R.string.sign_up_company_kpp), value = company.kpp)
            CompanyInfoRow(label = stringResource(R.string.sign_up_company_ogrn), value = company.ogrn)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLocationClick(company) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.show_location))
            }
        }
    }
}

@Composable
fun CompanyInfoRow(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(80.dp) // Фиксированная ширина для лейбла
        )
        Text(
            text = value ?: "-", // Показываем "-", если значение null или пустое
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Добавьте строки в res/values/strings.xml
/*

// Строки для ИНН/КПП/ОГРН уже должны быть
*/