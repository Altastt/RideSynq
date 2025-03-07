package com.example.ridesynq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.ridesynq.data.DataProfileScreen
import com.example.ridesynq.data.ProfileItem

class ProfileScreenVM: ViewModel() {
    // Список профилей
    private val _profileList: MutableState<List<ProfileItem>> = mutableStateOf(generateProfileList())
    val profileList: MutableState<List<ProfileItem>> = _profileList

    // Генерация списка профилей
    private fun generateProfileList(): List<ProfileItem> {
        val profiles = mutableListOf<ProfileItem>()
        repeat(DataProfileScreen.nicknameList.size) { index ->
            val id = index.toString()
            val url = DataProfileScreen.avatarList[index]
            val followersCount = (1..100).random() // Для примера, случайное количество подписчиков
            val followingCount = (1..100).random() // Для примера, случайное количество подписок
            val nickname = DataProfileScreen.nicknameList[index]
            val profile = ProfileItem(id, url, nickname, followersCount, followingCount)
            profiles.add(profile)
        }
        return profiles
    }
}