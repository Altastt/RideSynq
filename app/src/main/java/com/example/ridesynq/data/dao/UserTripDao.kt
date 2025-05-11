package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.ridesynq.data.entities.UserTrip

@Dao
interface UserTripDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userTrip: UserTrip)


}