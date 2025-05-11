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
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(
    navController: NavController,
    authViewModel: AuthVM,
    companyViewModel: CompanyViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val selectedCompany by companyViewModel.selectedCompany.collectAsState()

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
                selectedCompany != null -> {
                    CompanyInfoCard(company = selectedCompany!!)
                }
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
    company: Company,
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
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

