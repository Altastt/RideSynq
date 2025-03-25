package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.BackButton
import com.example.ridesynq.view.TextFieldCustom
import com.example.ridesynq.view.TextFieldEmail
import com.example.ridesynq.view.TextFieldPass
import com.example.ridesynq.view.navigation.AuthScreen
import com.example.ridesynq.viewmodel.AuthVM
import kotlinx.coroutines.launch


@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthVM,

) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val secondPasswordState = remember { mutableStateOf("") }
    var passwordsMatchState by remember { mutableStateOf(false) }

    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val surname = remember { mutableStateOf("") }

    DisposableEffect(authViewModel) {

        val observerEmailState = Observer<String> { _emailState ->
            emailState.value = _emailState
        }
        authViewModel.emailState.observeForever(observerEmailState)

        val observerPasswordState = Observer<String> { _passwordState ->
            passwordState.value = _passwordState
        }
        authViewModel.passwordState.observeForever(observerPasswordState)

        val observerSecondPasswordState = Observer<String> { _secondPasswordState ->
            secondPasswordState.value = _secondPasswordState
        }
        authViewModel.secondPasswordState.observeForever(observerSecondPasswordState)
        onDispose {
            authViewModel.emailState.removeObserver(observerEmailState)
            authViewModel.passwordState.removeObserver(observerPasswordState)
            authViewModel.secondPasswordState.removeObserver(observerSecondPasswordState)
        }
    }
    var checked by remember {
        mutableStateOf(false)
    }
    val updatePasswordMatchState: () -> Unit = {
        passwordsMatchState = authViewModel.checkPasswordMatch(passwordState.value, secondPasswordState.value)
    }

    Column(
        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) { BackButton(navController) }

        AutoresizedText(
            stringResource(R.string.sign_up_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 50.dp)
        )
        // Поля ФИО
        TextFieldCustom(
            stringResource(R.string.sign_up_lastname),
            lastName,
            onValueChange = { newValue -> lastName.value = newValue }
        )
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldCustom(
            stringResource(R.string.sign_up_firstname),
            firstName,
            onValueChange = { newValue -> firstName.value = newValue }
        )
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldCustom(
            stringResource(R.string.sign_up_surname),
            surname,
            onValueChange = { newValue -> surname.value = newValue }
        )
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldEmail(
            stringResource(R.string.sign_in_email),
            emailState,
            onValueChange = { newValue -> emailState.value = newValue })
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldPass(
            stringResource(R.string.sign_in_password),
            passwordState = passwordState,
            secondPasswordState = secondPasswordState,
            onValueChange = { newValue -> passwordState.value = newValue }
        )
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldPass(
            stringResource(R.string.sign_in_retrypassword),
            secondPassword = true,
            passwordState,
            secondPasswordState = secondPasswordState,
            onValueChange = {
                newValue -> secondPasswordState.value = newValue
                updatePasswordMatchState()
            },
        )
        Row(
            modifier = Modifier.padding(start = 30.dp, top = 30.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { _checked ->
                    checked = _checked
                },
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp)
            )
            Text(
                stringResource(R.string.sign_up_confidence),
                style = MaterialTheme.typography.titleSmall
            )
        }
        Button(onClick = {
            coroutineScope.launch {
                when (val result = authViewModel.registerUser(
                    firstName.value,
                    lastName.value,
                    surname.value,
                    emailState.value,
                    passwordState.value
                )) {
                    is AuthVM.RegistrationResult.Success -> navController.popBackStack()
                    is AuthVM.RegistrationResult.Error -> {
                        errorMessage.value = result.message
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        },
            modifier = Modifier.padding(bottom = 10.dp)) {
            AutoresizedText(
                stringResource(R.string.sign_up_button),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Button(onClick = {
            navController.navigate(AuthScreen.CompanyRegistration.route)
        }) {
            AutoresizedText(
                stringResource(R.string.sign_up_company_button),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

}