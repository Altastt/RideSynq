package com.example.ridesynq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_trip",
    primaryKeys = ["user_id", "trip_id"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id"), Index("trip_id")]
)
data class UserTrip(
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "trip_id")
    val tripId: Int,

    @ColumnInfo(name = "role")
    val role: String

)