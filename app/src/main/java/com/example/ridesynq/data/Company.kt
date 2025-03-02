package com.example.ridesynq.data

import com.yandex.mapkit.geometry.Point

data class Company(
    val id: Int,
    val name: String,
    val location: Point, // Координаты офиса компании
    val workTime: String = "09:00-18:00" // Пример дополнительного поля
)
