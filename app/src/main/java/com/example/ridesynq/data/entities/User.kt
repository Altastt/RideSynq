package com.example.ridesynq.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["company_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("company_id"), Index("post_id")]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val company_id: Int,
    val post_id: Int,
    val firstname: String,
    val lastname: String,
    val surname: String?,
    val phone: String,
    val transport_name: String?,
    val transport_number: String?
)