package com.example.ridesynq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    foreignKeys = [ForeignKey(
        entity = Company::class,
        parentColumns = ["id"],
        childColumns = ["company_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("company_id")]
)
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "company_id")
    val companyId: Int,

    val datetime: Long,


    @ColumnInfo(name = "start_latitude")
    val startLatitude: Double,

    @ColumnInfo(name = "start_longitude")
    val startLongitude: Double,

    @ColumnInfo(name = "end_latitude")
    val endLatitude: Double,

    @ColumnInfo(name = "end_longitude")
    val endLongitude: Double,

    @ColumnInfo(name = "is_to_work")
    val isToWork: Boolean,


    @ColumnInfo(name = "driver_id", index = true)
    val driverId: Int?,

    @ColumnInfo(name = "seats_available")
    val seatsAvailable: Int?,

    @ColumnInfo(name = "price_per_seat")
    val pricePerSeat: Double?,

    @ColumnInfo(name = "status")
    val status: String = "pending"

)