package com.example.ridesynq.view


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ridesynq.ui.theme.RideSynqTheme
import com.example.ridesynq.view.navigation.MainNavigation
import com.example.ridesynq.view.navigation.RootNavigation
import com.example.ridesynq.viewmodel.AuthVM


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkThemeNow = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(darkThemeNow) }
            RideSynqTheme (darkTheme = darkTheme) {
                RootNavigation(navController = rememberNavController(), onThemeUpdated = { darkTheme = !darkTheme })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(onThemeUpdated: () -> Unit, authViewModel: AuthVM) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val topBarState = rememberSaveable { (mutableStateOf(true)) }
    val bottomBarState = rememberSaveable { mutableStateOf(true) }


    when (navBackStackEntry?.destination?.route) {
        "home" -> {
            topBarState.value = true
            bottomBarState.value = true
        }

        "books" -> {
            topBarState.value = true
            bottomBarState.value = true
        }

        "camerainprofile" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "camerainmain" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "camera" -> {
            topBarState.value = false
            bottomBarState.value = false
        }

        "bookmarks" -> {
            topBarState.value = true
            bottomBarState.value = true
        }

        "profile" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "post" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "cameraforprofile" -> {
            topBarState.value = false
            bottomBarState.value = false
        }
        else -> {
            topBarState.value = false
            bottomBarState.value = false
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { AnimatedTopNavigationBar(navController, topBarState, scrollBehavior, viewModel()) },
        content = {
                MainNavigation(navController, onThemeUpdated, authViewModel)
        },
        bottomBar = { AnimatedBottomNavigationBar(navController, bottomBarState) },
    )
}