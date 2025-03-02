package com.example.ridesynq.models

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время отправления") },
        text = {
            TimePicker(
                state = state,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                val selectedTime = LocalTime.of(state.hour, state.minute)
                onTimeSelected(selectedTime)
            }) {
                Text("Подтвердить", fontSize = 20.sp)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена", fontSize = 20.sp)
            }
        }
    )
}