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
import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import com.example.ridesynq.BuildConfig
import com.example.ridesynq.utils.FileUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Helper function to create image Uri for camera
fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.externalCacheDir ?: context.cacheDir // Use cache dir
    val imageFile = File.createTempFile(
        imageFileName, ".jpg", storageDir
    )
    // Use authority matching your provider path in AndroidManifest.xml (if needed)
    // Typically: "${BuildConfig.APPLICATION_ID}.provider"
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider", // IMPORTANT: Match authority
        imageFile
    )
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onThemeUpdated: () -> Unit,
    authViewModel: AuthVM
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var newlySelectedUri by remember { mutableStateOf<Uri?>(null) }// Uri from camera before confirming
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // Final Uri to display/upload

    // --- Permission States ---
    // Camera Permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    // Storage Permission (adapt based on SDK level)
    val readMediaPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE // For older versions
    )

    // --- ActivityResultLaunchers ---
    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { newlySelectedUri = it } // Use the Uri we provided
            } else {
                // Handle failure or cancellation
                Toast.makeText(context, "Фото не сделано", Toast.LENGTH_SHORT).show()
                tempImageUri = null // Clear temp uri
            }
        }
    )

    // Gallery Launcher (Modern Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                newlySelectedUri = uri
            } else {
                Toast.makeText(context, "Фото не выбрано", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // --- Effect to process newly selected Uri ---
    LaunchedEffect(newlySelectedUri) {
        val uriToProcess = newlySelectedUri
        val user = currentUser // Capture current user state
        if (uriToProcess != null && user != null) {
            // Show loading indicator (optional)
            // isLoading = true

            // Copy the file and update the user profile
            val copiedFile = FileUtils.copyUriToInternalStorage(context, uriToProcess, user.id)
            if (copiedFile != null) {
                val result = authViewModel.updateUserAvatarPath(copiedFile.absolutePath)
                result.onFailure { error ->
                    Toast.makeText(context, "Не удалось сохранить аватар: ${error.message}", Toast.LENGTH_SHORT).show()
                    // Optionally delete the copied file if DB update failed
                    copiedFile.delete()
                }
            } else {
                Toast.makeText(context, "Не удалось скопировать изображение", Toast.LENGTH_SHORT).show()
            }

            // Clear the trigger and loading state
            newlySelectedUri = null
            // isLoading = false
        }
    }


    // --- UI ---
    Scaffold(
        topBar = { /* ... TopAppBar ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // --- Profile Header ---
            currentUser?.let { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AsyncImage(
                            // Display selected local URI OR stored URL OR placeholder
                            model = user.avatarUrl ?: R.drawable.profile,
                            contentDescription = stringResource(R.string.avatar),
                            placeholder = painterResource(id = R.drawable.profile_settings), // Placeholder while loading
                            error = painterResource(id = R.drawable.profile), // Error placeholder
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                        )
                        // Button to change photo
                        IconButton(
                            onClick = { showImageSourceDialog = true }, // Show selection dialog
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        // ... User Name and Login ...
                        Text(
                            text = if (!user.firstname.isNullOrBlank()) "${user.lastname} ${user.firstname}" else user.login,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.login,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // --- EDIT PROFILE BUTTON ---
                    IconButton(onClick = {
                        // Navigate to Edit Profile Screen
                        navController.navigate(SettingsScreen.EditProfile.route) // <<< CHANGE
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_profile))
                    }
                }
            } ?: run { /* ... Loading Placeholder ... */ }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Settings Menu ---
            SettingsMenuGroup(title = stringResource(R.string.settings_group_main)) {
                // ... Other SettingsItems ...
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
                    icon = painterResource(id = R.drawable.company),
                    text = stringResource(R.string.company),
                    onClick = { navController.navigate(SettingsScreen.RCompany.route) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Logout Button ---
            OutlinedButton(
                onClick = {
                    authViewModel.logoutUser()
                    navController.navigate(GraphRoute.AUTHENTICATION) {
                        popUpTo(GraphRoute.MAIN) { inclusive = true }
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
    // --- Image Source Selection Dialog ---
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Выберите источник") },
            text = { Text("Откуда загрузить фото?") },
            confirmButton = {
                // Camera Button
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (cameraPermissionState.status.isGranted) {
                        val newUri: Uri = createImageUri(context)
                        tempImageUri = newUri
                        cameraLauncher.launch(newUri)
                    } else {
                        cameraPermissionState.launchPermissionRequest() // Request permission
                    }
                }) { Text("Камера") }
            },
            dismissButton = {
                // Gallery Button
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (readMediaPermissionState.status.isGranted) {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        readMediaPermissionState.launchPermissionRequest() // Request permission
                    }
                }) { Text("Галерея") }
            }
        )
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
