package com.example.ridesynq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["company_id"],
            onDelete = ForeignKey.CASCADE // или SET_NULL, если пользователь может остаться без компании
        ),

    ],
    indices = [Index("company_id"), Index("post_id"), Index("login")] // Добавили индекс для login
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val company_id: Int,
    val post_id: Int, // 0 = Admin, >0 = Regular User Post ID
    val firstname: String?, // Сделали nullable
    val lastname: String?, // Сделали nullable
    val surname: String?, // Уже nullable
    val phone: String?, // Сделали nullable
    val transport_name: String?,
    val transport_number: String?,
    val transport_color: String?,

    @ColumnInfo(name = "login")
    val login: String,

    @ColumnInfo(name = "password")
    val password: String // ПОМНИТЕ О ХЭШИРОВАНИИ!
)