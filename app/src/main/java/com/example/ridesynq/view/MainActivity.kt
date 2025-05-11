package com.example.ridesynq.view


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // << Убедитесь, что импортирован
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // << Импорт для rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.repositories.CompanyRepository
import com.example.ridesynq.data.repositories.UserRepository
import com.example.ridesynq.ui.theme.RideSynqTheme
import com.example.ridesynq.view.navigation.GraphRoute
import com.example.ridesynq.view.navigation.RootNavigation
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.AuthVMFactory
import com.example.ridesynq.viewmodel.CompanyVMFactory
import com.example.ridesynq.viewmodel.CompanyViewModel
import com.yandex.mapkit.MapKitFactory

class MainActivity : ComponentActivity() {
    private val appDatabase by lazy { AppDatabase.getDatabase(this) }
    private val userRepository by lazy { UserRepository(appDatabase.userDao(), appDatabase.companyDao()) }
    private val companyRepository by lazy { CompanyRepository(appDatabase.companyDao()) }
    private val authVM: AuthVM by viewModels { AuthVMFactory(userRepository) }
    private val companyVM: CompanyViewModel by viewModels { CompanyVMFactory(companyRepository) }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkThemeNow = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(darkThemeNow) }
            val rootNavController = rememberNavController()
            RideSynqTheme(darkTheme = darkTheme) {
                val navBackStackEntry by rootNavController.currentBackStackEntryAsState()

                // Состояние для управления видимостью BottomBar (для анимации)
                val bottomAppBarState = rememberSaveable { mutableStateOf(true) }

                // Обновляем состояние bottomAppBarState на основе текущего роута
                LaunchedEffect(navBackStackEntry) {
                    bottomAppBarState.value = navBackStackEntry?.destination?.parent?.route == GraphRoute.MAIN
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Используем bottomAppBarState для управления компонентом
                        AnimatedBottomNavigationBar(
                            navController = rootNavController,
                            bottomAppBarState = bottomAppBarState // << Передаем состояние
                        )
                    },
                    content = { paddingValues ->
                        RootNavigation(
                            modifier = Modifier.padding(paddingValues),
                            navController = rootNavController,
                            onThemeUpdated = { darkTheme = !darkTheme },
                            authVM = authVM,
                            companyVM = companyVM
                        )
                    }
                )
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


