package com.example.ridesynq.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.ridesynq.R
import com.example.ridesynq.view.navigation.AuthScreen // Для навигации на логин после выхода
import com.example.ridesynq.view.navigation.GraphRoute
import com.example.ridesynq.view.navigation.SettingsScreen
import com.example.ridesynq.viewmodel.AuthVM
import com.yandex.runtime.image.ImageProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onThemeUpdated: () -> Unit,
    authViewModel: AuthVM
) {
    val currentUser by authViewModel.currentUser.collectAsState() // Подписываемся на StateFlow
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Для Toast или других действий

    // Структура экрана с использованием стандартных компонентов
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) }, // Добавим заголовок
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Цвет фона TopAppBar
                    titleContentColor = MaterialTheme.colorScheme.onSurface // Цвет текста
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Применяем отступы от Scaffold
                .verticalScroll(scrollState) // Делаем содержимое скроллящимся
                .padding(bottom = 32.dp) // Отступ снизу для кнопки Выход
        ) {
            // --- Секция профиля ---
            currentUser?.let { user -> // Отображаем только если пользователь загружен
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AsyncImage(
                            model = null, // TODO: Добавить URL аватара пользователя, если есть
                            contentDescription = stringResource(R.string.avatar),
                            placeholder = painterResource(id = R.drawable.profile), // Заглушка
                            error = painterResource(id = R.drawable.profile_settings), // Заглушка при ошибке
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        // Кнопка добавления/изменения фото
                        IconButton(
                            onClick = { /* TODO: Implement photo selection */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp) // Небольшое смещение
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant) // Фон для видимости
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_photo),
                                contentDescription = stringResource(R.string.add_or_change_photo),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            // Отображаем ФИО или email, если ФИО нет (для админа)
                            text = if (!user.firstname.isNullOrBlank()) "${user.lastname} ${user.firstname}" else user.login,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.login, // Отображаем email
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Кнопка редактирования профиля (опционально)
                    IconButton(onClick = { /* TODO: Navigate to profile edit screen */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_profile))
                    }
                }
            } ?: run {
                // Placeholder или индикатор загрузки, если currentUser еще null
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .padding(16.dp)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Секция меню настроек ---
            SettingsMenuGroup(title = stringResource(R.string.settings_group_main)) {
                SettingsItem(
                    icon = painterResource(R.drawable.theme),
                    text = stringResource(R.string.theme),
                    onClick = onThemeUpdated
                )
                SettingsItem(
                    icon = painterResource(R.drawable.profile_settings),
                    text = stringResource(R.string.profile_settings),
                    onClick = { navController.navigate(SettingsScreen.ProfileSettings.route) }
                )
                SettingsItem(
                    icon = painterResource(R.drawable.add_car),
                    text = stringResource(R.string.add_car),
                    onClick = { navController.navigate(SettingsScreen.AddCar.route) }
                )

                SettingsItem(
                    icon = painterResource(id = R.drawable.company), // Используем вашу иконку
                    text = stringResource(R.string.company),
                    onClick = { navController.navigate(SettingsScreen.RCompany.route) }
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Занимает оставшееся место

            // --- Кнопка Выход ---
            OutlinedButton(
                onClick = {
                    authViewModel.logoutUser()
                    // Переходим на граф аутентификации и очищаем весь стек назад
                    navController.navigate(GraphRoute.AUTHENTICATION) {
                        popUpTo(GraphRoute.MAIN) { inclusive = true } // Очищаем стек основного графа
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.logout))
            }
        }
    }
}

// Вспомогательный компонент для группы настроек
@Composable
fun SettingsMenuGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}


@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = { Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null) } // Стрелка по умолчанию
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = text) },
        trailingContent = trailingContent,
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface) // Цвет фона элемента списка
    )
    // Divider(modifier = Modifier.padding(start = 56.dp)) // Разделитель внутри группы (опционально)
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    text: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = { Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null) }
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = text, modifier = Modifier.size(24.dp)) }, // Устанавливаем размер для Painter
        trailingContent = trailingContent,
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
