package com.example.ridesynq.ui.routes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RoutesScreen(viewModel: RoutesViewModel = hiltViewModel()) {
    val routes by viewModel.routes.collectAsState()

    LazyColumn {
        items(routes) { route ->
            Text(text = "${route.startPoint} → ${route.endPoint}")
        }
    }
}