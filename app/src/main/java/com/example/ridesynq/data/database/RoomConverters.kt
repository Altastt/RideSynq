package com.example.ridesynq.data.database

import androidx.room.TypeConverter
import java.util.Date

class RoomConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}