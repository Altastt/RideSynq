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
            onDelete = ForeignKey.CASCADE
        ),
        // ForeignKey for post_id if Post table exists and is used for roles
        // ForeignKey(entity = Post::class, ...)
    ],
    indices = [Index("company_id"), /*Index("post_id"),*/ Index("login", unique = true)] // Made login unique
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val company_id: Int,
    val post_id: Int, // 0 = Admin, >0 = Regular User Post ID (Consider a dedicated Role table/enum)
    val firstname: String?,
    val lastname: String?,
    val surname: String?,
    val phone: String?,
    val transport_name: String?,
    val transport_number: String?,
    val transport_color: String?,

    @ColumnInfo(name = "login")
    val login: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "home_address_street")
    val homeAddressStreet: String? = null,

    @ColumnInfo(name = "home_address_city")
    val homeAddressCity: String? = null,

    @ColumnInfo(name = "home_address_postal_code")
    val homeAddressPostalCode: String? = null,

    @ColumnInfo(name = "home_address_latitude")
    val homeAddressLatitude: Double? = null,

    @ColumnInfo(name = "home_address_longitude")
    val homeAddressLongitude: Double? = null
)