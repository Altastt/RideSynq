package com.example.ridesynq.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.navigation.GraphRoute
import com.example.ridesynq.viewmodel.AuthVM
import com.example.ridesynq.viewmodel.ProfileScreenVM


@Composable
fun ProfileScreen(
    navController: NavController,
    onThemeUpdated: () -> Unit,
    viewModel: ProfileScreenVM = viewModel(),
    authViewModel: AuthVM
) {
    val profileList = viewModel.profileList.value
    val firstProfile = profileList.firstOrNull()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 22.dp, end = 22.dp)
    ) {
        Button(
            onClick = { navController.navigate(GraphRoute.PROFILE) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
            shape = RoundedCornerShape(30.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = firstProfile?.url, "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp, start = 15.dp)
                        .size(65.dp)
                        .clip(CircleShape)
                )

                firstProfile?.nickname?.let {
                    AutoresizedText(
                        it,
                        modifier = Modifier.padding(start = 50.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        IconButton(
            onClick = { },//navController.navigate(NavigationItems.CameraInProfile.route) },
            modifier = Modifier
                .padding(end = 15.dp)
                .size(35.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add_photo),
                "Add_photo",
            )
        }



        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
                .clip(RoundedCornerShape(15))
        ) {
            val menuItems = listOf(
                MenuItem(R.drawable.theme, "Theme", stringResource(R.string.theme)),
                MenuItem(
                    R.drawable.profile_settings,
                    "Profile Settings",
                    stringResource(R.string.profile_settings)
                ),
                MenuItem(
                    R.drawable.notification,
                    "Notification",
                    stringResource(R.string.notification)
                ),
                MenuItem(R.drawable.safety, "Security", stringResource(R.string.security)),
                MenuItem(R.drawable.company, "Company", stringResource(R.string.company)),
                MenuItem(R.drawable.faq, "FAQ", stringResource(R.string.faq)),
            )

            menuItems.forEach { menuItem ->
                MenuButton(
                    iconId = menuItem.iconId,
                    contentDescription = menuItem.contentDescription,
                    text = menuItem.text,
                    onThemeUpdated,
                    navController
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    iconId: Int,
    contentDescription: String,
    text: String,
    onThemeUpdated: () -> Unit,
    navController: NavController
) {
    Button(
        onClick = {
            when (contentDescription) {
                "Theme" -> {
                    onThemeUpdated()
                }

                "Profile Settings" -> {
                    //navController.navigate(.ProfileSettings.route)
                }

                "Notification" -> {
                  //  navController.navigate(SettingsScreen.Notification.route)
                }

                "Security" -> {
                    //navController.navigate(SettingsScreen.Security.route)
                }

                "Language" -> {
                   // navController.navigate(SettingsScreen.Language.route)
                }

                "FAQ" -> {
                   // navController.navigate(SettingsScreen.FAQ.route)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(start = 25.dp)
                    .size(35.dp)
            )
            AutoresizedText(
                text = text,
                modifier = Modifier.padding(start = 20.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

data class MenuItem(val iconId: Int, val contentDescription: String, val text: String)
data class ProfileSettingsItem(val contentDescription: String, val text: String)
data class NotificationSettingsItem(val contentDescription: String, val text: String)
data class SecuritySettingsItem(val contentDescription: String, val text: String)
data class LanguageSettingsItem(val contentDescription: String, val text: String)
data class FAQSettingsItem(val contentDescription: String, val text: String)