package com.example.ridesynq.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ridesynq.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp) // Отступы для списка
        ) {
            // Опция смены Email
            SettingsItem(
                icon = Icons.Filled.Email,
                text = stringResource(R.string.change_email),
                onClick = {
                    // TODO: Реализовать навигацию на экран смены Email
                    // navController.navigate("change_email_screen")
                    println("Navigate to Change Email Screen")
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Опция смены Пароля
            SettingsItem(
                icon = Icons.Filled.Lock,
                text = stringResource(R.string.change_password),
                onClick = {
                    // TODO: Реализовать навигацию на экран смены Пароля
                    // navController.navigate("change_password_screen")
                    println("Navigate to Change Password Screen")
                }
            )
        }
    }
}

// Можно использовать тот же SettingsItem, что и в ProfileScreen, если он вынесен
// Если нет, скопируйте его сюда или создайте общий файл для UI компонентов

/*
// Вспомогательный компонент для элемента настроек (если не вынесен)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = { Icon(Icons.Filled.ChevronRight, contentDescription = null) }
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = text) },
        trailingContent = trailingContent,
        modifier = Modifier.clickable(onClick = onClick).fillMaxWidth().padding(horizontal = 16.dp), // Добавил padding
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
*/

