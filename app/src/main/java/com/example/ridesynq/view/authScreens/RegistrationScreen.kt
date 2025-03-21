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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.ridesynq.viewmodel.AuthVM



@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthVM,

) {
    val context = LocalContext.current

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val secondPasswordState = remember { mutableStateOf("") }
    var passwordsMatchState by remember { mutableStateOf(false) }

    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val surname = remember { mutableStateOf("") }

    val checkEmailPass = stringResource(R.string.check_email_pass)
    val checkPrivacyPolicy = stringResource(R.string.check_privacypolicy)
    val matchPass = stringResource(R.string.match_pass)
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
                .padding(bottom = 80.dp)
        ) { BackButton(navController) }
        // Поля ФИО
        TextField(
            value = firstName.value,
            onValueChange = { firstName.value = it },
            label = { Text("Имя") }
        )

        TextField(
            value = lastName.value,
            onValueChange = { lastName.value = it },
            label = { Text("Фамилия") }
        )

        TextField(
            value = surname.value,
            onValueChange = { surname.value = it },
            label = { Text("Отчество") },

        )
        AutoresizedText(
            stringResource(R.string.sign_up_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 110.dp)
        )

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
            modifier = Modifier.padding(start = 30.dp, top = 30.dp),
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
            viewModelScope.launch {
                when(val result = authViewModel.registerUser(
                    firstName.value,
                    lastName.value,
                    surname.value,
                    emailState.value,
                    passwordState.value
                )) {
                    is Result.Success -> navController.popBackStack()
                    is Result.Failure -> showError(result.exception.message)
                }
            }
        }) {
            Text("Зарегистрироваться")
        }

    }

}