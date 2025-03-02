package com.example.ridesynq.view


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ridesynq.ui.theme.RideSynqTheme
import com.example.ridesynq.view.navigation.MainNavigation
import com.example.ridesynq.view.navigation.RootNavigation
import com.example.ridesynq.viewmodel.AuthVM
import com.yandex.mapkit.MapKitFactory


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

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

}


@RequiresApi(Build.VERSION_CODES.O)
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
        "trip" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "search" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        "profile" -> {
            topBarState.value = false
            bottomBarState.value = true
        }

        else -> {
            topBarState.value = false
            bottomBarState.value = false
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { AnimatedTopNavigationBar(navController, topBarState, scrollBehavior) },
        content = {
                MainNavigation(navController, onThemeUpdated, authViewModel)
        },
        bottomBar = { AnimatedBottomNavigationBar(navController, bottomBarState) },
    )
}