package com.example.ridesynq.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.entities.Post
import com.example.ridesynq.data.entities.User

data class UserWithDetails(
    @Embedded val user: User,
    @Relation(
        parentColumn = "company_id",
        entityColumn = "id"
    )
    val company: Company,
    @Relation(
        parentColumn = "post_id",
        entityColumn = "id"
    )
    val post: Post
)