package com.example.ridesynq.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText


@Composable
fun TripScreen(

) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Нет поездок", modifier = Modifier.padding(top = 170.dp, bottom = 370.dp))
        Button(
            onClick = {

            },
            shape = RoundedCornerShape(30),
            modifier = Modifier
                .width(180.dp)
                .height(50.dp)
        ) {
            AutoresizedText(
                stringResource(R.string.create_trip),
                style = MaterialTheme.typography.labelMedium
            )
        }


    }
}