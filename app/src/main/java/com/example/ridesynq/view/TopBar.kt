package com.example.ridesynq.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopNavigationBar(
    navController: NavController,
    topAppBarState: MutableState<Boolean>,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    AnimatedVisibility(
        visible = topAppBarState.value,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        TopBar(navController, scrollBehavior)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint
@Composable
fun TopBar(navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {

    TopAppBar(
        title = {

        },
        actions = {


        },
        scrollBehavior = scrollBehavior
    )
}