package com.example.ridesynq.view.authScreens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.TextFieldEmail
import com.example.ridesynq.view.TextFieldPass
import com.example.ridesynq.view.navigation.AuthScreen
import com.example.ridesynq.view.navigation.GraphRoute
import com.example.ridesynq.viewmodel.AuthVM



@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthVM,
    //retrofitViewModel: RetrofitVM = viewModel()
) {
    val context = LocalContext.current
  //  val authApi = retrofitViewModel.retrofit.create(AuthApi::class.java)
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val checkEmailPass = stringResource(R.string.check_email_pass)
    // Забираем значения из вьюмодели
    DisposableEffect(authViewModel) {

        val observerEmailState = Observer<String> { _emailState ->
            emailState.value = _emailState
        }
        authViewModel.emailState.observeForever(observerEmailState)

        val observerPasswordState = Observer<String> { _passwordState ->
            passwordState.value = _passwordState
        }
        authViewModel.passwordState.observeForever(observerPasswordState)
        onDispose {
            authViewModel.emailState.removeObserver(observerEmailState)
            authViewModel.passwordState.removeObserver(observerPasswordState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AutoresizedText(
            stringResource(R.string.sign_in_title),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 120.dp, bottom = 80.dp)
        )

        TextFieldEmail(
            stringResource(R.string.sign_in_email),
            emailState,
            onValueChange = { newValue -> emailState.value = newValue })
        Spacer(modifier = Modifier.height(15.dp))
        TextFieldPass(
            stringResource(R.string.sign_in_password),
            passwordState = passwordState,
            secondPasswordState = null,
            onValueChange = { newValue -> passwordState.value = newValue })

        Text(
            stringResource(R.string.sign_in_forgotpass),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 10.dp, bottom = 80.dp)
                .clickable { navController.navigate(AuthScreen.Forgot.route) }
        )
        Button(
            onClick = {
                if (emailState.value != "" && passwordState.value != "") {
                  //  CoroutineScope(Dispatchers.IO).launch {
                    //    authViewModel.authorization(emailState.value, passwordState.value, authApi)
                  //  }
                    navController.navigate(GraphRoute.MAIN) {
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(
                        context,
                        checkEmailPass,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            shape = RoundedCornerShape(30),
            modifier = Modifier
                .width(140.dp)
                .height(70.dp)
        ) {
            AutoresizedText(
                stringResource(R.string.sign_in_button),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Row(
            modifier = Modifier.padding(top = 40.dp),
        ) {
            AutoresizedText(
                stringResource(R.string.sign_in_text_to_signup),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(end = 5.dp)
            )

            Text(stringResource(R.string.sign_in_tb_to_signup),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate(AuthScreen.Registration.route)
                }
            )
        }

    }

}