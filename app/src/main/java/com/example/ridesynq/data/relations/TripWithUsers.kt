package com.example.ridesynq.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip

data class TripWithUsers(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserTrip::class,
            parentColumn = "trip_id",
            entityColumn = "user_id"
        )
    )
    val users: List<User>
)