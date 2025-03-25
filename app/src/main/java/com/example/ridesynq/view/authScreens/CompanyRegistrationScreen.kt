package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ridesynq.view.TextFieldCustom
import com.example.ridesynq.viewmodel.CompanyViewModel

@Composable
fun CompanyRegistrationScreen(
    navController: NavController,
    companyViewModel: CompanyViewModel
) {
    val context = LocalContext.current
    val companyName = remember { mutableStateOf("") }
    val inn = remember { mutableStateOf("") }
    val kpp = remember { mutableStateOf("") }
    val ogrn = remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) { BackButton(navController) }
        AutoresizedText(
            stringResource(R.string.sign_up_company_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 50.dp)
        )
            TextFieldCustom(
                stringResource(R.string.sign_up_company_name),
                companyName,
                onValueChange = { newValue -> companyName.value = newValue }
            )
            Spacer(modifier = Modifier.height(15.dp))
            TextFieldCustom(
                stringResource(R.string.sign_up_company_inn),
                inn,
                onValueChange = { newValue -> inn.value = newValue }
            )
            Spacer(modifier = Modifier.height(15.dp))
            TextFieldCustom(
                stringResource(R.string.sign_up_company_kpp),
                kpp,
                onValueChange = { newValue -> kpp.value = newValue }
            )
            Spacer(modifier = Modifier.height(15.dp))
            TextFieldCustom(
                stringResource(R.string.sign_up_company_ogrn),
                ogrn,
                onValueChange = { newValue -> ogrn.value = newValue }
            )
            Spacer(modifier = Modifier.height(25.dp))
            Button(onClick = {
                if (companyName.value.isNotBlank() && inn.value.isNotBlank()) {
                    companyViewModel.insertCompany(
                        Company(
                            name = companyName.value,
                            inn = inn.value,
                            kpp = kpp.value,
                            ogrn = ogrn.value,
                            latitude = 0.0, // Можно добавить карту для выбора
                            longitude = 0.0
                        )
                    )
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Заполните обязательные поля", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                AutoresizedText(
                    stringResource(R.string.sign_up_button),
                    style = MaterialTheme.typography.labelMedium
                )
            }
    }
}